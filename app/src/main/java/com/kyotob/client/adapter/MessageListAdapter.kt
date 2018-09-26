package com.kyotob.client.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.kyotob.client.entities.GetMessageResponse
import com.kyotob.client.view.MessageView
import com.kyotob.client.view.MyMessageView

class MessageListAdapter(private val context: Context): BaseAdapter() {
    var messages: Array<GetMessageResponse> = emptyArray()
    var icons: HashMap<String, String> = HashMap()
    var myName: String = ""

    override fun getCount(): Int = messages.size

    override fun getItem(position: Int): Any? = messages[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        if(messages[position].userName == myName) { // 自分のメッセージ
            return ((convertView as? MyMessageView) ?: MyMessageView(context)).apply {
                setMessage(messages[position])
                setImage(icons[messages[position].userName])
            }
        } else { // 相手のメッセージ
            return ((convertView as? MessageView) ?: MessageView(context)).apply {
                setMessage(messages[position])
                setImage(icons[messages[position].userName])
            }
        }
    }
}
