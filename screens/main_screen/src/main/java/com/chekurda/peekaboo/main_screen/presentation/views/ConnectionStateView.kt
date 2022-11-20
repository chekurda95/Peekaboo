package com.chekurda.peekaboo.main_screen.presentation.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.chekurda.common.half
import com.chekurda.design.custom_view_tools.TextLayout
import com.chekurda.design.custom_view_tools.utils.MeasureSpecUtils.measureDirection
import com.chekurda.design.custom_view_tools.utils.dp
import com.chekurda.design.custom_view_tools.utils.safeRequestLayout
import com.chekurda.peekaboo.main_screen.R
import com.chekurda.peekaboo.main_screen.presentation.views.drawables.AnimatedDotsDrawable

internal class ConnectionStateView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    enum class State(val text: String, @DrawableRes val backgroundRes: Int) {
        GAME_STARTED("Game is started!".uppercase(), R.drawable.connected_state_background),
        READY("READY".uppercase(), R.drawable.connected_state_background),
        SEARCHING_GAME_MASTER("Searching game master".uppercase(), R.drawable.search_state_background),
        SEARCHING_PLAYERS("Searching players".uppercase(), R.drawable.search_state_background)
    }

    private val textLayout = TextLayout {
        paint.apply {
            color = Color.WHITE
            typeface = Typeface.DEFAULT_BOLD
            textSize = dp(17).toFloat()
        }
    }

    private val dotsDrawable = AnimatedDotsDrawable().apply {
        callback = this@ConnectionStateView
        params = AnimatedDotsDrawable.DotsParams(size = dp(3))
        textColor = Color.WHITE
    }
    private val dotsSpacing = dp(2)

    var state: State = State.GAME_STARTED
        set(value) {
            field = value
            val isChanged = textLayout.configure { text = value.text }
            if (isChanged) {
                background = ContextCompat.getDrawable(context, value.backgroundRes)
                safeRequestLayout()
            }
        }

    init {
        outlineProvider = ViewOutlineProvider.BACKGROUND
        clipToOutline = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        dotsDrawable.setVisible(
            state == State.SEARCHING_GAME_MASTER || state == State.SEARCHING_PLAYERS,
            false
        )
        setMeasuredDimension(
            measureDirection(widthMeasureSpec) { suggestedMinimumWidth },
            measureDirection(heightMeasureSpec) { suggestedMinimumHeight },
        )
    }

    override fun getSuggestedMinimumWidth(): Int =
        textLayout.width + paddingStart + paddingEnd + if (dotsDrawable.isVisible) dotsSpacing + dotsDrawable.intrinsicWidth else 0

    override fun getSuggestedMinimumHeight(): Int =
        textLayout.height + paddingTop + paddingBottom

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (dotsDrawable.isVisible) {
            textLayout.layout(
                paddingStart + (measuredWidth - paddingStart - paddingEnd - textLayout.width - dotsDrawable.intrinsicWidth).half,
                paddingTop + (measuredHeight - paddingTop - paddingBottom - textLayout.height).half
            )
            val dotsLeft = textLayout.right + dotsSpacing
            val dotsTop = textLayout.top + textLayout.baseline - dotsDrawable.intrinsicHeight
            dotsDrawable.setBounds(
                dotsLeft,
                dotsTop,
                dotsLeft + dotsDrawable.intrinsicWidth,
                dotsTop + dotsDrawable.intrinsicHeight
            )
        } else {
            textLayout.layout(
                paddingStart + (measuredWidth - paddingStart - paddingEnd - textLayout.width).half,
                paddingTop + (measuredHeight - paddingTop - paddingBottom - textLayout.height).half
            )
        }
    }

    override fun onDraw(canvas: Canvas) {
        textLayout.draw(canvas)
        dotsDrawable.draw(canvas)
    }

    override fun verifyDrawable(who: Drawable): Boolean =
        who == dotsDrawable || super.verifyDrawable(who)
}