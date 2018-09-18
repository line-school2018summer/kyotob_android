package com.kyotob.client.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.kyotob.client.entities.Room
import com.kyotob.client.view.RoomView

// RoomListAdapterは引数にContextをとり、RoomViewに利用する
class RoomListAdapter(private val context: Context) : BaseAdapter() {
    // Roomオブジェクトのリストをプロパティroomとして持つ(最初は空)
    // アクセス修飾子はpublicなので外部から更新できる
    var rooms: List<Room> = emptyList()

    override fun getCount(): Int = rooms.size
    override fun getItem(position: Int): Any? = rooms[position]
    override fun getItemId(p0: Int): Long = 0

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View =
            ((convertView as? RoomView) ?: RoomView(context)).apply{
                setRoom(rooms[position])
    }
}