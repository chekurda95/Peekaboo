package com.chekurda.peekaboo.main_screen.domain

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.chekurda.common.storeIn
import com.chekurda.peekaboo.main_screen.data.Message
import com.chekurda.peekaboo.main_screen.utils.SimpleReceiver
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.SerialDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.Exception

internal class PlayerBluetoothManager {

    private var secureUUID = UUID.fromString(MASTER_SECURE_UUID)
    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    private var deviceBundleSubject = PublishSubject.create<Bundle>()
    private val bluetoothDeviceSubject = deviceBundleSubject.map { extras ->
        extras.getParcelable<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
    }.filter { device -> !device.name.isNullOrBlank() }

    private val searchReceiver = SimpleReceiver(action = BluetoothDevice.ACTION_FOUND) {
        deviceBundleSubject.onNext(it.extras!!)
    }
    private val searchStartReceiver = SimpleReceiver(
        action = BluetoothAdapter.ACTION_DISCOVERY_STARTED,
        isSingleEvent = true
    ) {
        Log.i("PlayerBluetoothManager", "Receiver On search started")
        listener?.onSearchStateChanged(isRunning = true)
        context?.let(searchEndReceiver::register)
    }
    private val searchEndReceiver = SimpleReceiver(
        action = BluetoothAdapter.ACTION_DISCOVERY_FINISHED,
        isSingleEvent = true
    ) {
        Log.i("PlayerBluetoothManager", "Receiver On search end")
        listener?.onSearchStateChanged(isRunning = false)
        context?.let(searchReceiver::unregister)
    }

    private val deviceListDisposable = SerialDisposable()
    private var connectionDisposable = SerialDisposable()
    private val disposer = CompositeDisposable().apply {
        add(deviceListDisposable)
        add(connectionDisposable)
    }

    private val messageStoreList: MutableList<Message> = mutableListOf()

    @Volatile
    private var isConnected: Boolean = false
    private val isSearching: Boolean
        get() = bluetoothAdapter.isDiscovering

    private var context: Context? = null
    private var mainHandler: Handler? = null
    private var socket: BluetoothSocket? = null

    var listener: BluetoothManagerListener? = null

    fun init(context: Context, mainHandler: Handler) {
        this.context = context
        this.mainHandler = mainHandler
        bluetoothAdapter.enable()
    }

    fun startPineLoverSearching() {
        val context = context ?: return
        if (bluetoothAdapter.scanMode != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            val discoverableIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
            }
            context.startActivity(discoverableIntent)
        }
        Log.d("PlayerBluetoothManager", "startPineLoverSearching")
        stopPineLoverSearching()
        subscribeOnDevices()
        searchStartReceiver.register(context)
        searchReceiver.register(context)
        bluetoothAdapter.startDiscovery()
    }

    private fun stopPineLoverSearching() {
        val context = context ?: return
        Log.d("PlayerBluetoothManager", "stopPineLoverSearching")
        deviceListDisposable.set(null)
        bluetoothAdapter.cancelDiscovery()
        searchReceiver.unregister(context)
        searchStartReceiver.unregister(context)
        searchEndReceiver.unregister(context)
    }

    private fun connectToPineLover(pineLover: BluetoothDevice) {
        Log.d("PlayerBluetoothManager", "connectToPineLover")
        if (isSearching) stopPineLoverSearching()
        isConnected = true
        Single.fromCallable {
            val bluetoothDevice = bluetoothAdapter.getRemoteDevice(pineLover.address)
            val socket = bluetoothDevice.createRfcommSocketToServiceRecord(secureUUID)
            try {
                socket.apply { connect() }
            } catch (ex: Exception) {
                socket.close()
                throw Exception()
            }
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    Log.d("PlayerBluetoothManager", "onSocketConnected")
                    listener?.onConnectionSuccess()
                    addSocketObserver(it)
                }, {
                    Log.e("PlayerBluetoothManager", "Socket error ${it.message}\n${it.stackTraceToString()}")
                    isConnected = false
                    listener?.onConnectionCanceled(isError = false)
                }
            ).storeIn(connectionDisposable)
    }

    private fun addSocketObserver(socket: BluetoothSocket) {
        this.socket = socket
        val thread = object : Thread() {

            override fun run() {
                super.run()
                kotlin.runCatching {
                    var allMessagesDelivered = false
                    val inputStream = ObjectInputStream(socket.inputStream)
                    val outputStream = ObjectOutputStream(socket.outputStream)
                    while (isConnected) {
                        when {
                            socket.inputStream.available() != 0 -> {
                                val obj = inputStream.readObject()
                                if (obj is Message) {
                                    messageStoreList.add(obj)
                                    outputStream.writeObject(obj)
                                }
                            }
                            !allMessagesDelivered -> {
                                outputStream.writeObject(messageStoreList)
                                allMessagesDelivered = true
                            }
                            else -> socket.outputStream.write(ByteArray(0))
                        }
                        Log.i("PlayerBluetoothManager", "success write")
                    }
                }.apply {
                    Log.d("PlayerBluetoothManager", "onSocketDisconnected")
                    closeSocket()
                    isConnected = false
                    mainHandler?.post {
                        startPineLoverSearching()
                        listener?.onConnectionCanceled(isError = true)
                    }
                }
            }
        }
        thread.start()
    }

    fun disconnect() {
        Log.d("PlayerBluetoothManager", "disconnect")
        closeSocket()
        if (!isConnected) return
        isConnected = false
        listener?.onConnectionCanceled(isError = false)
    }

    private fun subscribeOnDevices() {
        Log.d("PlayerBluetoothManager", "subscribeOnDevices")
        bluetoothDeviceSubject.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { bluetoothDevice ->
                Log.i("PlayerBluetoothManager", "on some device found")
                if (!isConnected && bluetoothDevice?.name?.contains(PLAYER_DEVICE_NAME) == true) {
                    connectToPineLover(bluetoothDevice)
                }
            }.storeIn(deviceListDisposable)
    }

    fun clear() {
        Log.d("PlayerBluetoothManager", "clear")
        context = null
        mainHandler = null
        isConnected = false
        closeSocket()
    }

    fun release() {
        isConnected = false
        disposer.dispose()
        closeSocket()
    }

    private fun closeSocket() {
        try {
            socket?.close()
        } catch (ignore: Exception) { }
        socket = null
    }
}