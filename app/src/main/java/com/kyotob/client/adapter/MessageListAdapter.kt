package com.kyotob.client.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.kyotob.client.entities.GetMessageResponse
import com.kyotob.client.view.MessageView

class MessageListAdapter(private val context: Context): BaseAdapter() {
    var messages: Array<GetMessageResponse> = emptyArray()

    override fun getCount(): Int = messages.size

    override fun getItem(position: Int): Any? = messages[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View =
            ((convertView as? MessageView) ?: MessageView(context)).apply {
                setMessage(messages[position])
            }
}
