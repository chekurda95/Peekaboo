package com.chekurda.peekaboo.main_screen.presentation

import android.app.Activity
import android.os.Handler
import com.chekurda.common.base_fragment.BasePresenter
import com.chekurda.peekaboo.main_screen.data.Message
import com.chekurda.peekaboo.main_screen.presentation.views.user.panel.MessagePanelController

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

        fun updateMessageList(messageList: List<Message>)
    }

    /**
     * Контракт презентера главного экрана.
     */
    interface Presenter : BasePresenter<View>, MessagePanelController {

        fun onPineModeSelected()

        fun onUserModeSelected()
    }
}