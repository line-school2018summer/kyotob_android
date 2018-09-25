package com.kyotob.client.view

import android.content.Context
import android.support.v4.content.res.ResourcesCompat
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.kyotob.client.CircleTransform
import com.kyotob.client.R
import com.kyotob.client.baseUrl
import com.kyotob.client.entities.GetMessageResponse
import com.squareup.picasso.Picasso

class MessageView(context: Context) : FrameLayout(context) {
    private var profileImageView: ImageView

    private var userNameTextView: TextView

    private var contentView: TextView

    private var imageView: ImageView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_message, this)
        profileImageView = findViewById(R.id.profile_image_view)
        userNameTextView = findViewById(R.id.user_name_view)
        contentView = findViewById(R.id.content_view)
        imageView = findViewById(R.id.content_image_view)
    }

    fun setMessage(message: GetMessageResponse) {
        userNameTextView.text = message.userScreenName
        if (message.contentType == "image") {
            contentView.visibility = View.INVISIBLE
            Picasso.get().load("$baseUrl/image/download/${message.content}").into(imageView)
        } else {
            imageView.visibility = View.INVISIBLE
            contentView.text = message.content
        }
    }

    fun setImage(url: String?) {
        if (url != null) {
            Picasso.get().load("$baseUrl/image/download/$url").transform(CircleTransform()).into(profileImageView)
        } else {
            Picasso.get().load("foo").placeholder(R.drawable.boy).transform(CircleTransform()).into(profileImageView)
        }
    }
}
