package com.chekurda.peekaboo.main_screen.presentation.views.user.panel

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import com.chekurda.design.custom_view_tools.utils.MeasureSpecUtils.makeExactlySpec
import com.chekurda.design.custom_view_tools.utils.MeasureSpecUtils.makeUnspecifiedSpec
import com.chekurda.design.custom_view_tools.utils.MeasureSpecUtils.measureDirection
import com.chekurda.design.custom_view_tools.utils.dp
import com.chekurda.design.custom_view_tools.utils.layout
import com.chekurda.peekaboo.main_screen.R

internal class MessagePanel @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ViewGroup(context, attrs) {

    private val sendButtonSize = dp(45)
    private val sendButtonHorizontalSpacing = dp(6)

    val inputView = AppCompatEditText(context).apply {
        background = ContextCompat.getDrawable(context, R.drawable.input_background)
        maxLines = 5
        minHeight = sendButtonSize
        hint = "Enter message"
        updatePadding(left = dp(15), right = dp(15))
    }
    val sendButton = AppCompatButton(context).apply {
        background = ContextCompat.getDrawable(context, R.drawable.send_ripple_button_background)
        outlineProvider = ViewOutlineProvider.BACKGROUND
        clipToOutline = true
    }

    init {
        addView(inputView)
        addView(sendButton)
        setBackgroundColor(ContextCompat.getColor(context, R.color.message_panel_background_color))
        updatePadding(left = dp(15), right = dp(15), top = dp(5), bottom = dp(5))
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val availableWidth = MeasureSpec.getSize(widthMeasureSpec) - paddingStart - paddingEnd

        val sendSpec = makeExactlySpec(sendButtonSize)
        sendButton.measure(sendSpec, sendSpec)

        val inputWidth = availableWidth - sendButton.measuredWidth - sendButtonHorizontalSpacing
        inputView.measure(makeExactlySpec(inputWidth), makeUnspecifiedSpec())

        setMeasuredDimension(
            measureDirection(widthMeasureSpec) { suggestedMinimumWidth },
            measureDirection(heightMeasureSpec) { suggestedMinimumHeight }
        )
    }

    override fun getSuggestedMinimumHeight(): Int =
        super.getSuggestedMinimumHeight()
            .coerceAtLeast(inputView.measuredHeight + paddingTop + paddingBottom)

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        inputView.layout(paddingStart, paddingTop)
        sendButton.layout(
            measuredWidth - paddingEnd - sendButton.measuredWidth,
            inputView.bottom - sendButton.measuredHeight
        )
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        inputView.isEnabled = enabled
        sendButton.isEnabled = enabled
    }
}