package com.chekurda.peekaboo.main_screen.presentation.views.user.list

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chekurda.common.base_list.calculateDiff
import com.chekurda.peekaboo.main_screen.data.Message

internal class MessageListAdapter : RecyclerView.Adapter<MessageViewHolder>() {

    var messageList: MutableList<Message> = mutableListOf()
        private set

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.recycledViewPool.setMaxRecycledViews(INCOME_MESSAGE_VIEW_HOLDER_TYPE, 100)
    }

    fun setDataList(dataList: List<Message>) {
        val diffResult = calculateDiff(messageList, dataList)
        messageList.clear()
        messageList.addAll(dataList)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder =
        when (viewType) {
            INCOME_MESSAGE_VIEW_HOLDER_TYPE -> MessageViewHolder(parent.context, isOutcome = false)
            OUTCOME_MESSAGE_VIEW_HOLDER_TYPE -> MessageViewHolder(parent.context, isOutcome = true)
            else -> throw IllegalArgumentException("Unsupported view holder type $viewType")
        }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messageList[position])
    }

    override fun getItemCount(): Int = messageList.size
    override fun getItemViewType(position: Int): Int =
        if (messageList[position].isOutcome == true) OUTCOME_MESSAGE_VIEW_HOLDER_TYPE
        else INCOME_MESSAGE_VIEW_HOLDER_TYPE
}

private const val INCOME_MESSAGE_VIEW_HOLDER_TYPE = 1
private const val OUTCOME_MESSAGE_VIEW_HOLDER_TYPE = 2