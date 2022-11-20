package com.chekurda.peekaboo.main_screen.domain

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.util.Log
import com.chekurda.common.storeIn
import com.chekurda.peekaboo.main_screen.data.GameStatus
import com.chekurda.peekaboo.main_screen.data.PlayerFoundEvent
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.SerialDisposable
import io.reactivex.schedulers.Schedulers
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.lang.Exception
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

internal class MasterBluetoothManager {

    private var secureUUID = UUID.fromString(MASTER_SECURE_UUID)
    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    private val serviceDisposable = SerialDisposable()
    private val discoverableDisposable = SerialDisposable()
    private val disposer = CompositeDisposable().apply {
        add(serviceDisposable)
        add(discoverableDisposable)
    }

    @Volatile
    private var isConnected: Boolean = false
    private var isDiscoverable: Boolean = false

    private var outputStream: ObjectOutputStream? = null

    private var originBluetoothName = ""

    private var context: Context? = null
    private var mainHandler: Handler? = null

    @Volatile
    private lateinit var gameStatus: GameStatus
    private val rssiMap = ConcurrentHashMap<String, Int>()
    private val gatts = ConcurrentHashMap<String, BluetoothGatt>()

    var listener: BluetoothManagerListener? = null
    var rssiListener: ((Int) -> Unit)? = null

    fun init(context: Context, mainHandler: Handler) {
        this.context = context
        this.mainHandler = mainHandler
        bluetoothAdapter.enable()
    }

    fun startPlayerSearchingService() {
        if (isConnected) return
        gameStatus = GameStatus(false, emptyList())
        Log.d("MasterBluetoothManager", "startPlayerSearchingService")
        if (!isDiscoverable) makeDiscoverable()
        bluetoothAdapter.startDiscovery()
        prepareDeviceName()
        openPlayersSearchingService()
    }

    private fun makeDiscoverable() {
        val context = context ?: return
        if (bluetoothAdapter.scanMode != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            val discoverableIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVERABLE_SECONDS)
            }
            context.startActivity(discoverableIntent)
            Observable.timer(DISCOVERABLE_SECONDS, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    isDiscoverable = false
                    if (!isConnected) makeDiscoverable()
                }.storeIn(discoverableDisposable)
        }
        isDiscoverable = true
    }

    @Volatile
    private var serverSocket: BluetoothServerSocket? = null

    private fun openPlayersSearchingService() {
        if (context == null) return
        Log.d("MasterBluetoothManager", "openPlayersSearchingService")
        serverSocket?.close()
        serverSocket = null
        Single.fromCallable {
            val serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(MASTER_SERVICE_NAME, secureUUID)
            this.serverSocket = serverSocket
            var socket: BluetoothSocket? = null
            try {
                socket = serverSocket.accept()
            } catch (ex: Exception) {
                serverSocket?.close()
            }
            socket!!
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    Log.d("MasterBluetoothManager", "onSocketConnected")
                    startPlayersSocketObserver(it)
                    listener?.onConnectionSuccess()
                },
                {
                    Log.e("MasterBluetoothManager", "openPlayersSearchingService error ${it.message}\n${it.stackTraceToString()}")
                    isConnected = false
                    closeServerSocket()
                    listener?.onConnectionCanceled(isError = true)
                }
            )
            .storeIn(serviceDisposable)
    }

    private fun startPlayersSocketObserver(playersSocket: BluetoothSocket) {
        Log.e("TAGTAG", "startPlayersSocketObserver 1")
        outputStream = ObjectOutputStream(playersSocket.outputStream)
        val inputStream = ObjectInputStream(playersSocket.inputStream)
        val thread = object : Thread() {

            override fun run() {
                super.run()
                kotlin.runCatching {
                    Log.e("TAGTAG", "thread running")
                    isConnected = true
                    val connectionCheckArray = ByteArray(0)
                    while (isConnected) {
                        gatts.forEach { it.value.readRemoteRssi() }
                        if (playersSocket.inputStream.available() != 0) {
                            val obj = inputStream.readObject()
                            if (obj is PlayerFoundEvent) {
                                val playerName = obj.deviceName
                                val playerAddress = obj.deviceAddress
                                val newFoundList = mutableListOf<String>().apply { addAll(gameStatus.foundPlayers) }
                                newFoundList.add(playerName)
                                val playerGatt = gatts[playerAddress]
                                playerGatt?.close()
                                gatts.remove(playerAddress)
                                rssiMap.remove(playerAddress)
                                gameStatus = gameStatus.copy(foundPlayers = newFoundList)
                            }
                        } else {
                            playersSocket.outputStream.write(connectionCheckArray)
                        }
                        var maxRssi: Int = -1000
                        rssiMap.forEach { maxRssi = it.value.coerceAtLeast(maxRssi) }
                        mainHandler?.post {
                            rssiListener?.invoke(maxRssi)
                        }
                        sleep(1000)
                    }
                }.apply {
                    Log.d("MasterBluetoothManager", "onSocketDisconnected")
                    isConnected = false

                    playersSocket.close()
                    closeServerSocket()
                    mainHandler?.post {
                        this@MasterBluetoothManager.outputStream = null
                        listener?.onConnectionCanceled(isError = false)
                    }
                }
            }
        }
        thread.start()
    }

    fun disconnect() {
        Log.d("MasterBluetoothManager", "disconnect")
        bluetoothAdapter.name = originBluetoothName
        closeServerSocket()
        if (!isConnected) return
        isConnected = false
        listener?.onConnectionCanceled(isError = false)
    }

    fun clear() {
        Log.d("MasterBluetoothManager", "clear")
        context = null
        mainHandler = null
        isConnected = false
        closeServerSocket()
        bluetoothAdapter.name = originBluetoothName
    }

    fun release() {
        bluetoothAdapter.name = originBluetoothName
        isConnected = false
        closeServerSocket()
        disposer.dispose()
    }

    fun onGameStarted() {
        prepareGatts()
        val outputStream = outputStream ?: return
        val status = GameStatus(
            isStarted = true,
            foundPlayers = emptyList()
        )
        Completable.fromCallable {
            outputStream.writeObject(status)
        }.subscribeOn(Schedulers.io())
            .subscribe(
                { Log.d("MasterBluetoothManager", "onGameStarted sent") },
                { Log.d("MasterBluetoothManager", "onGameStarted send error $it") }
            )
            .storeIn(disposer)
    }

    private fun prepareGatts() {
        val cb = object : BluetoothGattCallback() {
            override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
                super.onReadRemoteRssi(gatt, rssi, status)
                val bg = gatt ?: return
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    this@MasterBluetoothManager.rssiMap[bg.device.address] = rssi
                }
            }

            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                super.onConnectionStateChange(gatt, status, newState)
                val bg = gatt ?: return
                this@MasterBluetoothManager.gatts[bg.device.address] = bg
                this@MasterBluetoothManager.rssiMap[bg.device.address] = -1000
            }
        }
        bluetoothAdapter.bondedDevices.forEach {
            it.connectGatt(context, true, cb)
        }
    }

    private fun prepareDeviceName() {
        originBluetoothName = bluetoothAdapter.name
        if (!originBluetoothName.contains(MATER_DEVICE_NAME)) {
            bluetoothAdapter.name = "%s %s".format(MATER_DEVICE_NAME, bluetoothAdapter.name)
        }
    }

    private fun closeServerSocket() {
        serverSocket?.close()
        serverSocket = null
        gatts.forEach { it.value.close() }
        gatts.clear()
        rssiMap.clear()
    }
}

internal const val MASTER_SECURE_UUID = "fa87c0d0-afac-11de-8a39-0800200c9a66"
internal const val MATER_DEVICE_NAME = "Peekaboo game master"
internal const val MASTER_SERVICE_NAME = "Peekaboo_game_service"
private const val DISCOVERABLE_SECONDS = 300L