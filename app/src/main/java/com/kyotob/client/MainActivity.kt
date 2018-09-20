package com.kyotob.client

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.kyotob.client.chatList.ChatListActivity
import com.kyotob.client.login.LoginActivity
import com.kyotob.client.register.RegisterActivity
import com.kyotob.client.setting.SettingActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val p = getSharedPreferences(USERDATAKEY, Context.MODE_PRIVATE)
        val editor = p.edit()
        editor.putString(USERNAMEKEY, "test")
        editor.putString(USERSCREENNAMEKEY, "Test User #1")
        editor.putString(TOKENKEY, "bar")
        editor.apply()
    }

    fun openRegisterActivity(view: View) {
        startActivity(Intent(this, RegisterActivity::class.java))
    }

    fun openLoginActivity(view: View) {
        startActivity(Intent(this, LoginActivity::class.java))
    }

    fun openChatActivity(view: View) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("ROOM_ID", 1)
        startActivity(intent)
    }

    fun openChatListActivity(view: View) {
        startActivity(Intent(this, ChatListActivity::class.java))
    }

    fun openSettingActivity(view: View) {
        startActivity(Intent(this, SettingActivity::class.java))
    }
}
