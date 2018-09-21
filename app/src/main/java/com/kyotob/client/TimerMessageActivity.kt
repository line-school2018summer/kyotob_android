package com.kyotob.client

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button

class TimerMessageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer_message_sender)

        findViewById<Button>(R.id.send_button).setOnClickListener {
            // 戻る
            finish()
        }
    }
}