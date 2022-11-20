package com.chekurda.peekaboo.app

import android.app.Application
import android.util.Log
import io.reactivex.plugins.RxJavaPlugins

/**
 * [Application] Peekaboo.
 */
class PeekabooApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        PluginSystem.initialize(this)
        RxJavaPlugins.setErrorHandler { error -> Log.e("RxError", "$error") }
    }
}