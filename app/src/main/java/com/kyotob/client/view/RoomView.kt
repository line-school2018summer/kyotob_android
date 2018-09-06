package com.kyotob.client.view

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.kyotob.client.R
import com.kyotob.client.bindView
import com.kyotob.client.entities.Room

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

    // 初期化
    init {
        // LayoutInflaterからのviewを生成
        LayoutInflater.from(context).inflate(R.layout.view_room, this)
    }

    fun setRoom(room: Room) {
        // 文字をセット
        userNameTextView?.text = room.userScreenName
        latestMessageTextView?.text = room.recentMessage

        // 画像をセットする
        profileImageView?.setBackgroundColor(Color.RED)
    }
}