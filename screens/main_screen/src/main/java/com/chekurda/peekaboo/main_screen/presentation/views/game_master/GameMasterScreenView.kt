package com.chekurda.peekaboo.main_screen.presentation.views.game_master

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.*
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams.MATCH_PARENT
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.core.view.updatePadding
import com.chekurda.design.custom_view_tools.utils.PAINT_MAX_ALPHA
import com.chekurda.design.custom_view_tools.utils.SimplePaint
import com.chekurda.design.custom_view_tools.utils.dp
import com.chekurda.peekaboo.main_screen.R
import com.chekurda.peekaboo.main_screen.presentation.views.ConnectionStateView
import com.chekurda.peekaboo.main_screen.presentation.views.drawables.RSSIDrawable
import kotlin.math.roundToInt

internal class GameMasterScreenView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val stateView = ConnectionStateView(context).apply {
        state = ConnectionStateView.State.SEARCHING_PLAYERS
        updatePadding(left = dp(25), right = dp(25), top = dp(15), bottom = dp(15))
    }

    private val startGameButton = AppCompatButton(context).apply {
        setTextSize(TypedValue.COMPLEX_UNIT_PX, dp(20).toFloat())
        setTextColor(Color.WHITE)
        text = "Start game"
        background = ContextCompat.getDrawable(context, R.drawable.outcome_message_background)
        setPadding(dp(20))
        isVisible = false
        setOnClickListener {
            controller.onGameStarted()
            isVisible = false
            state = ConnectionStateView.State.GAME_STARTED
            rssiDrawable.amplitude = 0f
        }
    }

    private val rssiDrawable = RSSIDrawable().apply {
        callback = this@GameMasterScreenView
        setVisible(false, false)
        minRadius = dp(20).toFloat()
        pulsationRadiusDx = dp(4).toFloat()
    }

    var state: ConnectionStateView.State
        get() = stateView.state
        set(value) {
            stateView.state = value
            startGameButton.isVisible = value == ConnectionStateView.State.READY
            rssiDrawable.setVisible(state == ConnectionStateView.State.GAME_STARTED, false)
        }

    lateinit var controller: GameMasterController

    init {
        setWillNotDraw(false)
        val stateViewLp = LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
            setMargins(dp(30), dp(30), dp(30), 0)
            gravity = Gravity.CENTER_HORIZONTAL
        }
        addView(stateView, stateViewLp)
        val startGameButtonLp = LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
            gravity = Gravity.CENTER
        }
        addView(startGameButton, startGameButtonLp)
        background = ContextCompat.getDrawable(context, R.drawable.main_screen_background)
    }

    fun updateRssi(rssi: Int) {
        rssiDrawable.amplitude = 1f - (-rssi / 40f).coerceAtLeast(0f).coerceAtMost(1f)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rssiDrawable.setBounds(0, 0, w, h)
        rssiDrawable.maxRadius = w / 2f - dp(30)
    }

    override fun onDraw(canvas: Canvas) {
        rssiDrawable.draw(canvas)
    }

    override fun verifyDrawable(who: Drawable): Boolean =
        who == rssiDrawable || super.verifyDrawable(who)
}