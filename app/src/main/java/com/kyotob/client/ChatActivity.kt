package com.kyotob.client

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class ChatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        title = "チャット"
    }
}
