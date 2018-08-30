package com.kyotob.client

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun openChatActivity(view: View) {
        startActivity(Intent(this, ChatActivity::class.java))
    }

    fun openChatListActivity(view: View) {
        startActivity(Intent(this, ChatListActivity::class.java))
    }

    fun openSettingActivity(view: View) {
        startActivity(Intent(this, SettingActivity::class.java))
    }
}
