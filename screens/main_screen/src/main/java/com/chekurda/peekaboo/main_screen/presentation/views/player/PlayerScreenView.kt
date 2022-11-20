package com.chekurda.peekaboo.main_screen.presentation.views.player

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import com.chekurda.common.half
import com.chekurda.design.custom_view_tools.utils.MeasureSpecUtils.makeUnspecifiedSpec
import com.chekurda.design.custom_view_tools.utils.MeasureSpecUtils.measureDirection
import com.chekurda.design.custom_view_tools.utils.dp
import com.chekurda.design.custom_view_tools.utils.layout
import com.chekurda.peekaboo.main_screen.R
import com.chekurda.peekaboo.main_screen.presentation.views.ConnectionStateView

internal class PlayerScreenView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ViewGroup(context, attrs) {

    private val connectionStateView = ConnectionStateView(context).apply {
        state = ConnectionStateView.State.SEARCHING_GAME_MASTER
        updatePadding(left = dp(45), right = dp(45), top = dp(15), bottom = dp(15))
    }
    private val connectionStateViewTopSpacing = dp(25)

    var state: ConnectionStateView.State
        get() = connectionStateView.state
        set(value) {
            connectionStateView.state = value
        }

    init {
        setWillNotDraw(false)
        addView(connectionStateView)
        background = ContextCompat.getDrawable(context, R.drawable.main_screen_background)
    }

    fun updateConnectionState(isConnected: Boolean) {
        state = if (isConnected) ConnectionStateView.State.READY else ConnectionStateView.State.SEARCHING_GAME_MASTER
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        connectionStateView.measure(makeUnspecifiedSpec(), makeUnspecifiedSpec())
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
    }
}