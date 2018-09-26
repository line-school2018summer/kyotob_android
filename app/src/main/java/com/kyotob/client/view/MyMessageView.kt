package com.kyotob.client.view

import android.content.Context
import android.support.v4.content.res.ResourcesCompat
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.kyotob.client.R
import com.kyotob.client.baseUrl
import com.kyotob.client.entities.GetMessageResponse
import com.squareup.picasso.Picasso

class MyMessageView(context: Context) : FrameLayout(context) {
    private var profileImageView: ImageView

    private var contentView: TextView

    private var imageView: ImageView

    init {
        LayoutInflater.from(context).inflate(R.layout.my_view_message, this)
        profileImageView = findViewById(R.id.my_profile_image_view)
        contentView = findViewById(R.id.my_content_view)
        imageView = findViewById(R.id.my_content_image_view)
    }

    fun setMessage(message: GetMessageResponse) {
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
            Picasso.get().load("$baseUrl/image/download/$url").into(profileImageView)
        } else {
            profileImageView.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.boy, null))
        }
    }
}
