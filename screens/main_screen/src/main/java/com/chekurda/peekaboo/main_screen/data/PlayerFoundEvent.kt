package com.chekurda.peekaboo.main_screen.data

import java.io.Serializable

data class PlayerFoundEvent(
    val deviceAddress: String,
    val deviceName: String
) : Serializable