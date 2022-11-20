package com.chekurda.peekaboo.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.chekurda.peekaboo.AppPlugin.gameFragmentFactory
import com.chekurda.peekaboo.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, gameFragmentFactory.createMainScreenFragment())
                .commit()
        }
    }
}
