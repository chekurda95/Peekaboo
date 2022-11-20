package com.chekurda.peekaboo.main_screen.data

import java.io.Serializable

internal data class GameStatus(
    val isStarted: Boolean,
    val foundPlayers: List<String>
) : Serializable