package com.chekurda.peekaboo.main_screen.domain

internal interface BluetoothManagerListener {
    fun onConnectionSuccess()
    fun onConnectionCanceled(isError: Boolean)
    fun onSearchStateChanged(isRunning: Boolean)
}