package com.vaibhavdhunde.android.practice.prochat.ui.chat

import com.vaibhavdhunde.android.practice.prochat.R
import com.vaibhavdhunde.android.practice.prochat.data.Message
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.item_date.*
import kotlinx.android.synthetic.main.item_date.view.*
import kotlinx.android.synthetic.main.item_date.view.item_date
import kotlinx.android.synthetic.main.item_text_msg_to.*
import java.text.SimpleDateFormat
import java.util.*

class DateItem(val time: Date) : Item() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        setDate(viewHolder)
    }

    private fun setDate(viewHolder: ViewHolder) {
        val date = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
            .format(time).toUpperCase()
        viewHolder.item_date.text = date
    }

    override fun getLayout(): Int {
        return R.layout.item_date
    }
}