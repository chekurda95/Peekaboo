package com.chekurda.peekaboo.main_screen.data

import com.chekurda.common.base_list.ComparableItem
import java.io.Serializable
import java.util.UUID

internal data class Message(
    val uuid: UUID,
    val senderName: String,
    val text: String
) : ComparableItem<Message>, Serializable {

    var isOutcome: Boolean? = null

    override fun areItemsTheSame(anotherItem: Message): Boolean =
        uuid == anotherItem.uuid

    override fun areContentsTheSame(anotherItem: Message): Boolean =
        this == anotherItem
}