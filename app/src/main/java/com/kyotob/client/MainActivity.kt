package com.kyotob.client

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.kyotob.client.setting.SettingActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun openRegisterActivity(view: View) {
        startActivity(Intent(this, RegisterActivity::class.java))
    }

    fun openLoginActivity(view: View) {
        startActivity(Intent(this, LoginActivity::class.java))
    }

    fun openChatActivity(view: View) {
        startActivity(Intent(this, ChatActivity::class.java))
    }

    fun openChatListActivity(view: View) {
        startActivity(Intent(this, ChatListActivity::class.java))
    }

    fun openSettingActivity(view: View) {
        val p = getSharedPreferences("userData", Context.MODE_PRIVATE)
        val editor = p.edit()
        editor.putString("name", "test")
        editor.putString("screenName", "Test User #1")
        editor.putString("accessToken", "bar")
        editor.apply()
        startActivity(Intent(this, SettingActivity::class.java))
    }
}
