package com.chekurda.common.base_fragment

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

/**
 * Базовая реализация презентера [BasePresenter].
 */
abstract class BasePresenterImpl<VIEW> : BasePresenter<VIEW>, LifecycleObserver {

    protected var view: VIEW? = null

    protected var isStarted: Boolean = false
    protected var isResumed: Boolean = false

    override fun attachView(view: VIEW) {
        this.view = view
    }

    override fun detachView() {
        view = null
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    open fun viewIsStarted() {
        isStarted = true
    }
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    open fun viewIsResumed() {
        isResumed = true
    }
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    open fun viewIsPaused() {
        isResumed = false
    }
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    open fun viewIsStopped() {
        isStarted = false
    }

    override fun onDestroy() = Unit
}