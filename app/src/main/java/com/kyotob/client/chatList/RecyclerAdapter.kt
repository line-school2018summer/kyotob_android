package com.kyotob.client.chatList

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.kyotob.client.R
import com.kyotob.client.entities.FriendItem

class RecyclerAdapter(val context: Context, val itemList: List<FriendItemForView>): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    val inflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.makegroup_member, p0, false))
    }

    override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
        if (!(p0 is ViewHolder)) return
        if (itemList.size <= p1) return
        //todo: imageView
        p0.screenNameView.text = itemList[p1].screenName
        p0.checkBox.setChecked(itemList.get(p1).isChecked);
        p0.checkBox.setOnCheckedChangeListener { buttonView, isChecked -> itemList.get(p1).isChecked = isChecked }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.icon_imageView)
        val checkBox: CheckBox = itemView.findViewById(R.id.check)
        val screenNameView: TextView = itemView.findViewById(R.id.screen_name)
    }
}