package com.kyotob.client.view

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.kyotob.client.R
import com.kyotob.client.baseUrl
import com.kyotob.client.bindView
import com.kyotob.client.database.RoomDatabaseHelper
import com.kyotob.client.database.RoomsMidokuModel
import com.kyotob.client.entities.Room
import com.squareup.picasso.Picasso

// 個々のRoomViewの雛形を作るつくるクラス
// RoomViewAdapterで利用する
class RoomView(context: Context): FrameLayout(context) {
//    無くてもいいんじゃね?
//    constructor(context: Context?,
//                attrs: AttributeSet?) : super(context, attrs)
//
//    constructor(context: Context?,
//                attrs: AttributeSet?,
//                defStyleAttr: Int) : super(context, attrs, defStyleAttr)
//
//    constructor(context: Context?,
//                attrs: AttributeSet?,
//                defStyleAttr: Int,
//                defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    // layoutファイルにある部品を変数(プロパティ)として持たせる
    // extensions.ktを使い、簡潔ににResourceIDからオブジェクトを見つけて、変数(プロパティ)に代入する
    private val profileImageView: ImageView by bindView(R.id.profile_image_view)
    private val userNameTextView: TextView by bindView(R.id.user_name_text_view)
    private val latestMessageTextView: TextView by bindView(R.id.latest_message_text_view)
    private val timeTextView: TextView by bindView(R.id.time_text)
    private val newMessageCounter: TextView by bindView(R.id.new_message_counter)

    // 初期化
    init {
        // LayoutInflaterからのviewを生成
        LayoutInflater.from(context).inflate(R.layout.view_room, this)
    }

    fun setRoom(room: Room) {
        // 表示名を変更
        userNameTextView.text = room.roomName
        // 最新のメッセージを変更
        if(room.recentMessage.matches(Regex(".*.png|.*.jpg|.*.jpeg"))) {
            latestMessageTextView.text = "画像が送信されました"
        } else {
            latestMessageTextView.text = room.recentMessage
        }

        // 時間を変更
        timeTextView.text = room.createdAt.toString().substring(11, 16)

        // 画像をセットする
        profileImageView.setBackgroundColor(Color.WHITE)
        Picasso.get().load(baseUrl + "/image/download/" + room.imageUrl).into(profileImageView)

        // ---------- SQLITE ----------------
        val roomDatabaseHelper = RoomDatabaseHelper(context) // インスタンス
        // データを検索
        val midokuNum = roomDatabaseHelper.searchData(room.roomId)
        if (midokuNum == -1 || midokuNum == 0) { // 新規Roomの場合
            val midokuModel = RoomsMidokuModel(room.roomId, 0) // データ
            roomDatabaseHelper.inserData(midokuModel) // データの挿入
            newMessageCounter.visibility = View.INVISIBLE // 表示
        } else {// 既存のRoomの場合
            newMessageCounter.text = midokuNum.toString() // 表示の更新
            newMessageCounter.visibility = View.VISIBLE // 表示
        }
        // ----------------------------------
    }
}