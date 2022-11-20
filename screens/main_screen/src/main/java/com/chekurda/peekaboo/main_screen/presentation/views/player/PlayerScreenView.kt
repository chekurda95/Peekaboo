package com.chekurda.peekaboo.main_screen.presentation.views.player

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.core.view.updatePadding
import com.chekurda.common.half
import com.chekurda.design.custom_view_tools.utils.MeasureSpecUtils.makeUnspecifiedSpec
import com.chekurda.design.custom_view_tools.utils.MeasureSpecUtils.measureDirection
import com.chekurda.design.custom_view_tools.utils.dp
import com.chekurda.design.custom_view_tools.utils.layout
import com.chekurda.peekaboo.main_screen.R
import com.chekurda.peekaboo.main_screen.data.GameStatus
import com.chekurda.peekaboo.main_screen.presentation.views.ConnectionStateView

internal class PlayerScreenView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val connectionStateView = ConnectionStateView(context).apply {
        state = ConnectionStateView.State.SEARCHING_GAME_MASTER
        updatePadding(left = dp(15), right = dp(15), top = dp(15), bottom = dp(15))
    }
    private val foundButton = AppCompatButton(context).apply {
        setTextSize(TypedValue.COMPLEX_UNIT_PX, dp(20).toFloat())
        setTextColor(Color.WHITE)
        text = "I was found"
        background = ContextCompat.getDrawable(context, R.drawable.outcome_message_background)
        setPadding(dp(20))
        isVisible = false
        setOnClickListener {
            controller.onFoundMe()
            isVisible = false
            state = ConnectionStateView.State.GAME_OVER
        }
    }
    private val connectionStateViewTopSpacing = dp(25)

    var state: ConnectionStateView.State
        get() = connectionStateView.state
        set(value) {
            connectionStateView.state = value
            foundButton.isVisible = state == ConnectionStateView.State.GAME_STARTED
        }

    lateinit var controller: PlayerController

    init {
        setWillNotDraw(false)
        addView(connectionStateView)
        val foundButtonLp = LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
            gravity = Gravity.CENTER
        }
        addView(foundButton, foundButtonLp)
        background = ContextCompat.getDrawable(context, R.drawable.main_screen_background)
    }

    fun updateConnectionState(isConnected: Boolean) {
        state = if (isConnected) ConnectionStateView.State.READY else ConnectionStateView.State.SEARCHING_GAME_MASTER
    }

    fun changeGameStatus(status: GameStatus) {
        if (status.isStarted) {
            state = ConnectionStateView.State.GAME_STARTED
            foundButton.isVisible = true
            Toast.makeText(context, "Don't let him trap you!!!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        connectionStateView.measure(makeUnspecifiedSpec(), makeUnspecifiedSpec())
        foundButton.measure(makeUnspecifiedSpec(), makeUnspecifiedSpec())
        setMeasuredDimension(
            measureDirection(widthMeasureSpec) { suggestedMinimumWidth },
            measureDirection(heightMeasureSpec) { suggestedMinimumHeight },
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        connectionStateView.layout(
            paddingStart + (measuredWidth - connectionStateView.measuredWidth).half,
            paddingTop + connectionStateViewTopSpacing
        )
        foundButton.layout(
            paddingStart + (measuredWidth - foundButton.measuredWidth).half,
            paddingTop + (measuredHeight - foundButton.measuredHeight).half
        )
    }
}