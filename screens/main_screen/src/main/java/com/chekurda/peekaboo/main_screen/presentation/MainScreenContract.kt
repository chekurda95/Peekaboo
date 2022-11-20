package com.chekurda.peekaboo.main_screen.presentation

import android.app.Activity
import android.os.Handler
import com.chekurda.common.base_fragment.BasePresenter

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
    }

    /**
     * Контракт презентера главного экрана.
     */
    interface Presenter : BasePresenter<View> {

        fun onMasterModeSelected()

        fun onPlayerModeSelected()

        fun onGameStarted()
    }
}