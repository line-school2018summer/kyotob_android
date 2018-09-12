package com.kyotob.client.view

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.kyotob.client.R
import com.kyotob.client.entities.GetMessageResponse

class MessageView(context: Context) : FrameLayout(context) {
    private var profileImageView: ImageView

    private var userNameTextView: TextView

    private var contentView: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_message, this)
        profileImageView = findViewById(R.id.profile_image_view)
        userNameTextView = findViewById(R.id.user_name_view)
        contentView = findViewById(R.id.content_view)
    }

    fun setMessage(message: GetMessageResponse) {
        userNameTextView.text = message.userScreenName
        contentView.text = message.content
    }
}
