package com.chekurda.peekaboo.main_screen.presentation.views.user.list

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import com.chekurda.design.custom_view_tools.TextLayout
import com.chekurda.design.custom_view_tools.utils.MeasureSpecUtils.measureDirection
import com.chekurda.design.custom_view_tools.utils.dp
import com.chekurda.design.custom_view_tools.utils.safeRequestLayout
import com.chekurda.peekaboo.main_screen.R
import com.chekurda.peekaboo.main_screen.data.Message

@SuppressLint("ViewConstructor") // Только для программного создания
internal class MessageItemView(
    context: Context,
    private val isOutcome: Boolean
) : View(context) {

    private val cloudBackground: Drawable =
        if (isOutcome) {
            R.drawable.outcome_message_background
        } else {
            R.drawable.income_message_background
        }.let { ContextCompat.getDrawable(context, it)!! }

    private val senderNameLayout = TextLayout {
        paint.apply {
            textSize = dp(20).toFloat()
            color = Color.BLACK
        }
        includeFontPad = false
        isVisible = !isOutcome
    }
    private val messageTextLayout = TextLayout {
        paint.apply {
            textSize = dp(20).toFloat()
            color = Color.WHITE
        }
        maxLines = Int.MAX_VALUE
        includeFontPad = false
    }
    private val cloudInnerPadding = dp(12)
    private val horizontalEmptySpace = dp(50)

    private lateinit var message: Message

    fun setMessage(message: Message) {
        this.message = message
        messageTextLayout.configure { text = message.text }
        senderNameLayout.configure { text = message.senderName }
        safeRequestLayout()
    }

    init {
        updatePadding(left = dp(10), right = dp(2), top = dp(3), bottom = dp(3))
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val availableWidth = MeasureSpec.getSize(widthMeasureSpec) - paddingStart - paddingEnd
        val maxTextWidth = availableWidth - cloudInnerPadding * 2 - horizontalEmptySpace
        senderNameLayout.configure { maxWidth = maxTextWidth }
        messageTextLayout.configure { maxWidth = maxTextWidth }
        setMeasuredDimension(
            measureDirection(widthMeasureSpec) { suggestedMinimumWidth },
            measureDirection(heightMeasureSpec) { suggestedMinimumHeight }
        )
    }

    override fun getSuggestedMinimumHeight(): Int =
        paddingTop + paddingBottom + senderNameLayout.height + messageTextLayout.height + cloudInnerPadding * 2

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (isOutcome) layoutOutcome() else layoutIncome()
    }

    override fun onDraw(canvas: Canvas) {
        cloudBackground.draw(canvas)
        senderNameLayout.draw(canvas)
        messageTextLayout.draw(canvas)
    }

    private fun layoutIncome() {
        senderNameLayout.layout(paddingStart + cloudInnerPadding, paddingTop + cloudInnerPadding)
        messageTextLayout.layout(senderNameLayout.left, senderNameLayout.bottom)
        val cloudWidth = senderNameLayout.width.coerceAtLeast(messageTextLayout.width) + cloudInnerPadding * 2
        val cloudHeight = senderNameLayout.height + messageTextLayout.height + cloudInnerPadding * 2
        cloudBackground.setBounds(
            paddingStart,
            paddingTop,
            paddingStart + cloudWidth,
            paddingTop + cloudHeight
        )
    }

    private fun layoutOutcome() {
        messageTextLayout.layout(
            measuredWidth - paddingEnd - cloudInnerPadding - messageTextLayout.width,
            paddingTop + cloudInnerPadding
        )
        val cloudWidth = messageTextLayout.width + cloudInnerPadding * 2
        val cloudHeight = messageTextLayout.height + cloudInnerPadding * 2
        val cloudStart = measuredWidth - paddingEnd - cloudWidth
        cloudBackground.setBounds(
            cloudStart,
            paddingTop,
            cloudStart + cloudWidth,
            paddingTop + cloudHeight
        )
    }
}