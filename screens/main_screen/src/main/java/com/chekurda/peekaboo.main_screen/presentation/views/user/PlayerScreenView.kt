package com.chekurda.peekaboo.main_screen.presentation.views.user

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chekurda.common.half
import com.chekurda.design.custom_view_tools.utils.MeasureSpecUtils.makeExactlySpec
import com.chekurda.design.custom_view_tools.utils.MeasureSpecUtils.makeUnspecifiedSpec
import com.chekurda.design.custom_view_tools.utils.MeasureSpecUtils.measureDirection
import com.chekurda.design.custom_view_tools.utils.dp
import com.chekurda.design.custom_view_tools.utils.layout
import com.chekurda.peekaboo.main_screen.R
import com.chekurda.peekaboo.main_screen.data.Message
import com.chekurda.peekaboo.main_screen.presentation.views.ConnectionStateView
import com.chekurda.peekaboo.main_screen.presentation.views.user.list.MessageListAdapter
import com.chekurda.peekaboo.main_screen.presentation.views.user.panel.MessagePanel
import com.chekurda.peekaboo.main_screen.presentation.views.user.panel.MessagePanelController
import org.apache.commons.lang3.StringUtils

internal class PlayerScreenView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ViewGroup(context, attrs) {

    private val adapter = MessageListAdapter()

    private val messagePanel = MessagePanel(context).apply {
        isEnabled = false
        sendButton.setOnClickListener { onSendButtonClicked() }
    }
    private val messageListView = RecyclerView(context).apply {
        adapter = this@PlayerScreenView.adapter
        layoutManager = object : LinearLayoutManager(context) {
            init {
                stackFromEnd = true
            }

            override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
                kotlin.runCatching { super.onLayoutChildren(recycler, state) }
            }
        }
        clipToPadding = false
        updatePadding(top = dp(10), bottom = dp(10))
    }
    private val connectionStateView = ConnectionStateView(context).apply {
        state = ConnectionStateView.State.SEARCH_PINE
        updatePadding(left = dp(45), right = dp(45), top = dp(15), bottom = dp(15))
    }
    private val connectionStateViewTopSpacing = dp(25)
    private var connectionAnimator: ValueAnimator? = null
    private var connectionStateRunnable = Runnable {
        changeStateVisibility(isVisible = state != ConnectionStateView.State.CONNECTED)
    }

    private lateinit var controller: MessagePanelController

    var state: ConnectionStateView.State
        get() = connectionStateView.state
        set(value) {
            connectionStateView.state = value
            removeCallbacks(connectionStateRunnable)
            postDelayed(connectionStateRunnable, CONNECTION_SHOWING_TIME_MS)
        }

    init {
        setWillNotDraw(false)
        addView(messageListView)
        addView(connectionStateView)
        addView(messagePanel)
        background = ContextCompat.getDrawable(context, R.drawable.main_screen_background)
    }

    fun attachController(controller: MessagePanelController) {
        this.controller = controller
    }

    fun updateConnectionState(isConnected: Boolean) {
        messagePanel.isEnabled = isConnected
        state = if (isConnected) ConnectionStateView.State.CONNECTED else ConnectionStateView.State.SEARCH_PINE
    }

    fun updateMessageList(messageList: List<Message>) {
        adapter.setDataList(messageList)
        // По-хорошему нужно подвязываться на видимость нижней ячейки, но чесн слово уже сил нет
        val needToScroll = true
        if (needToScroll) scrollToBottom()
        Log.d("PineBluetoothManager", "updateMessageList $messageList")
    }

    private fun changeStateVisibility(isVisible: Boolean) {
        if (isVisible == connectionStateView.isVisible) return
        connectionAnimator?.cancel()
        var translationDistance = 0
        connectionAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = STATE_ANIMATION_DURATION_MS
            if (isVisible) {
                connectionStateView.isVisible = true
                interpolator = DecelerateInterpolator()
                addUpdateListener {
                    connectionStateView.translationY = - translationDistance * (1f - animatedFraction)
                }
            } else {
                interpolator = AccelerateInterpolator()
                addUpdateListener {
                    connectionStateView.translationY = - translationDistance * animatedFraction
                }
                doOnEnd { connectionStateView.isVisible = false }
            }
            start()
            pause()
            doOnPreDraw {
                translationDistance = connectionStateView.height + connectionStateViewTopSpacing
                resume()
            }
        }
        invalidate()
    }

    private fun onSendButtonClicked() {
        messagePanel.apply {
            val text = inputView.text?.toString()
            if (text.isNullOrBlank()) return
            inputView.setText(StringUtils.EMPTY)
            controller.sendMessage(text)
        }
    }

    private fun scrollToBottom() {
        messageListView.scrollBy(0, Int.MAX_VALUE)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val availableWidth = MeasureSpec.getSize(widthMeasureSpec) - paddingStart - paddingEnd
        val availableHeight = MeasureSpec.getSize(heightMeasureSpec) - paddingTop - paddingBottom
        val childWidthSpec = makeExactlySpec(availableWidth)
        connectionStateView.measure(makeUnspecifiedSpec(), makeUnspecifiedSpec())
        messagePanel.measure(childWidthSpec, makeUnspecifiedSpec())
        messageListView.measure(
            childWidthSpec,
            makeExactlySpec(availableHeight - messagePanel.measuredHeight)
        )
        setMeasuredDimension(
            measureDirection(widthMeasureSpec) { suggestedMinimumWidth },
            measureDirection(heightMeasureSpec) { suggestedMinimumHeight },
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        messageListView.layout(paddingStart, paddingTop)
        messagePanel.layout(
            paddingStart,
            measuredHeight - paddingBottom - messagePanel.measuredHeight
        )
        connectionStateView.layout(
            paddingStart + (measuredWidth - connectionStateView.measuredWidth).half,
            paddingTop + connectionStateViewTopSpacing
        )
    }
}

private const val CONNECTION_SHOWING_TIME_MS = 500L
private const val STATE_ANIMATION_DURATION_MS = 250L