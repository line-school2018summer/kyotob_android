package com.kyotob.client

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
//import com.kyotob.client.chatList.ChatListActivity
import com.kyotob.client.login.LoginActivity
import com.kyotob.client.register.RegisterActivity
import com.kyotob.client.setting.SettingActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val p = getSharedPreferences(USER_DATA_KEY, Context.MODE_PRIVATE)
        val editor = p.edit()
        editor.putString(USER_NAME_KEY, "test")
        editor.putString(USER_SCREEN_NAME_KEY, "Test User #1")
        editor.putString(TOKEN_KEY, "bar")
        editor.apply()

        (findViewById<Button>(R.id.openChatActivity)).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        (findViewById<Button>(R.id.openLoginActivity)).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        (findViewById<Button>(R.id.openRegisterActivity)).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        (findViewById<Button>(R.id.openChatActivity)).setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("ROOM_ID", 1)
            startActivity(intent)
        }

        (findViewById<Button>(R.id.openChatListActivity)).setOnClickListener {
            startActivity(Intent(this, ChatListActivity::class.java))
        }

        (findViewById<Button>(R.id.openSettingActivity)).setOnClickListener {
            startActivity(Intent(this, SettingActivity::class.java))
        }
    }
}
