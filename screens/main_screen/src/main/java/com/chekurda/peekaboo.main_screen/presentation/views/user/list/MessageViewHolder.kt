package com.chekurda.peekaboo.main_screen.presentation.views.user.list

import android.content.Context
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.recyclerview.widget.RecyclerView
import com.chekurda.peekaboo.main_screen.data.Message

/**
 * ViewHolder списка сообщений.
 */
internal class MessageViewHolder private constructor(
    private val view: MessageItemView
) : RecyclerView.ViewHolder(view) {

    constructor(context: Context, isOutcome: Boolean) : this(MessageItemView(context, isOutcome))

    private lateinit var data: Message

    init {
        view.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    }

    fun bind(data: Message) {
        this.data = data
        view.setMessage(data)
    }
}