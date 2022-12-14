package com.chekurda.peekaboo.main_screen.presentation

import android.app.Activity
import android.os.Handler
import com.chekurda.common.base_fragment.BasePresenter
import com.chekurda.peekaboo.main_screen.data.GameStatus
import com.chekurda.peekaboo.main_screen.data.PlayerFoundEvent
import com.chekurda.peekaboo.main_screen.presentation.views.game_master.GameMasterController
import com.chekurda.peekaboo.main_screen.presentation.views.player.PlayerController

/**
 * Контракт главного экрана.
 */
internal interface MainScreenContract {

    /**
     * View контракт главного экрана.
     */
    interface View {

        /**
         * Изменить состояние поиска девайсов.
         */
        fun updateSearchState(isRunning: Boolean)

        fun updateConnectionState(isConnected: Boolean)

        /**
         * Предоставить Activity.
         */
        fun provideActivity(): Activity

        fun provideHandler(): Handler

        fun showMaxRssi(rssi: Int)

        fun onGameStatusChanged(status: GameStatus)

        fun onPlayerFound(event: PlayerFoundEvent)

        fun finishGame()
    }

    /**
     * Контракт презентера главного экрана.
     */
    interface Presenter : BasePresenter<View>, GameMasterController, PlayerController {

        fun onMasterModeSelected()

        fun onPlayerModeSelected()
    }
}