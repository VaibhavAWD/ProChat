package com.vaibhavdhunde.android.practice.prochat.ui.chat

import android.content.Context
import com.vaibhavdhunde.android.practice.prochat.R
import com.vaibhavdhunde.android.practice.prochat.data.TextMessage
import com.vaibhavdhunde.android.practice.prochat.util.FirestoreUtil
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.item_text_msg_to.*
import java.text.SimpleDateFormat
import java.util.*

class TextMessageItem(
    val message: TextMessage,
    val context: Context
) : Item() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.item_message.text = message.message
        setMessageTime(viewHolder)
    }

    private fun setMessageTime(viewHolder: ViewHolder) {
        val msgTime = SimpleDateFormat("h:mm a", Locale.getDefault())
            .format(message.time).toUpperCase()
        viewHolder.item_message_time.text = msgTime
    }

    override fun getLayout(): Int {
        if (message.senderId == FirestoreUtil.currentUserId) {
            return R.layout.item_text_msg_to
        } else {
            return R.layout.item_text_msg_from
        }
    }

    override fun isSameAs(other: com.xwray.groupie.Item<*>?): Boolean {
        if (other !is TextMessageItem) {
            return false
        }
        if (this.message != other.message) {
            return false
        }
        return true
    }

    override fun equals(other: Any?): Boolean {
        return isSameAs(other as? TextMessageItem)
    }

    override fun hashCode(): Int {
        var result = message.hashCode()
        result = 31 * result + context.hashCode()
        return result
    }
}