package com.chekurda.peekaboo.main_screen.data

internal data class GameStatus(
    val isStarted: Boolean,
    val foundPlayers: List<String>
)