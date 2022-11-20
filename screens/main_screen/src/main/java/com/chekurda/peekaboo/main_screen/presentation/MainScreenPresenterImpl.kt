package com.chekurda.peekaboo.main_screen.presentation

import com.chekurda.common.base_fragment.BasePresenterImpl
import com.chekurda.peekaboo.main_screen.domain.BluetoothManagerListener
import com.chekurda.peekaboo.main_screen.domain.MasterBluetoothManager
import com.chekurda.peekaboo.main_screen.domain.PlayerBluetoothManager

internal class MainScreenPresenterImpl : BasePresenterImpl<MainScreenContract.View>(),
    MainScreenContract.Presenter,
    BluetoothManagerListener {

    private var masterManager: MasterBluetoothManager? = null
    private var playerManager: PlayerBluetoothManager? = null
    private var isConnected: Boolean = false

    override fun attachView(view: MainScreenContract.View) {
        super.attachView(view)
        masterManager?.init(view.provideActivity().applicationContext, view.provideHandler())
        playerManager?.init(view.provideActivity().applicationContext, view.provideHandler())
    }

    override fun detachView() {
        super.detachView()
        masterManager?.clear()
        playerManager?.clear()
    }

    override fun onMasterModeSelected() {
        masterManager = MasterBluetoothManager().apply {
            init(view!!.provideActivity().applicationContext, view!!.provideHandler())
            listener = this@MainScreenPresenterImpl
            startPlayerSearchingService()
        }
    }

    override fun onPlayerModeSelected() {
        playerManager = PlayerBluetoothManager().apply {
            init(view!!.provideActivity().applicationContext, view!!.provideHandler())
            listener = this@MainScreenPresenterImpl
            startGameMasterSearching()
        }
    }

    override fun viewIsStarted() {
        super.viewIsStarted()
        if (!isConnected) {
            masterManager?.startPlayerSearchingService()
            playerManager?.startGameMasterSearching()
        }
    }

    override fun viewIsStopped() {
        super.viewIsStopped()
        if (isConnected) {
            masterManager?.disconnect()
            playerManager?.disconnect()
        }
    }

    override fun onSearchStateChanged(isRunning: Boolean) {
        view?.updateSearchState(isRunning)
        if (isStarted && !isRunning && !isConnected) {
            playerManager?.startGameMasterSearching()
        }
    }

    override fun onConnectionSuccess() {
        isConnected = true
        view?.updateConnectionState(isConnected = true)
    }

    override fun onConnectionCanceled(isError: Boolean) {
        isConnected = false
        view?.updateConnectionState(isConnected = false)
        if (isStarted) {
            masterManager?.startPlayerSearchingService()
            playerManager?.startGameMasterSearching()
        }
    }

    override fun onGameStarted() {
        masterManager?.onGameStarted()
    }

    override fun onDestroy() {
        super.onDestroy()
        playerManager?.release()
        masterManager?.release()
    }
}