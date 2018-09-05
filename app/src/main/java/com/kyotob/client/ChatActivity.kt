package com.kyotob.client

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView
import com.kyotob.client.adapter.MessageListAdapter
import com.kyotob.client.entities.Message
import java.sql.Timestamp

class ChatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        title = "チャット"

        val listAdapter = MessageListAdapter(applicationContext)
        listAdapter.messages = getDummyMessages()

        val listView = findViewById<ListView>(R.id.list_view)
        listView.adapter = listAdapter
    }

    private fun getDummyMessages() =
            mutableListOf(
                    Message(1, 1, 1, "foo", Timestamp(1536133785)),
                    Message(1, 2, 1, "bar", Timestamp(1536137278)))
}
