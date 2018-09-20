package com.kyotob.client

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.util.Log
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.google.gson.GsonBuilder
import com.kyotob.client.adapter.MessageListAdapter
import com.kyotob.client.chatList.ChatListActivity
import com.kyotob.client.entities.GetMessageResponse
import com.kyotob.client.entities.PostMessageRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class ChatActivity : AppCompatActivity() {

    private val timer = Timer()
    private lateinit var listAdapter: MessageListAdapter
    private lateinit var client: Client
    private lateinit var token: String
    private var roomId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        title = "チャット"

        roomId = intent.getIntExtra("ROOM_ID", -1)
        if (roomId == -1) {
            Toast.makeText(applicationContext, "ルームIDの取得に失敗しました", Toast.LENGTH_SHORT).show()
            finish()
        }

        val gson = GsonBuilder()
                //.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create()

        val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                // レスポンスからオブジェクトへのコンバータファクトリを設定する
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build()

        // クライアントの実装の生成
        client = retrofit.create(Client::class.java)

        listAdapter = MessageListAdapter(applicationContext)

        val sharedPreferences = getSharedPreferences(USERDATAKEY, Context.MODE_PRIVATE)
        token = sharedPreferences.getString(TOKENKEY, null) ?: throw Exception("token is null")
        val userName = sharedPreferences.getString(USERNAMEKEY, null) ?: throw Exception("userName is null")

        client.getMessages(roomId, token).enqueue(object : Callback<Array<GetMessageResponse>> {
            override fun onResponse(call: Call<Array<GetMessageResponse>>?, response: Response<Array<GetMessageResponse>>?) {
                val listView = findViewById<ListView>(R.id.list_view)
                listAdapter.messages = response?.body() ?: emptyArray()
                listView.adapter = listAdapter
            }

            override fun onFailure(call: Call<Array<GetMessageResponse>>?, t: Throwable?) {}
        })

        val submitButton = findViewById<Button>(R.id.submit)
        val textArea = findViewById<TextInputEditText>(R.id.message)

        submitButton.setOnClickListener {
            client.sendMessage(1, PostMessageRequest(userName, textArea.text.toString()), token)
                  .enqueue(object : Callback<Boolean> {
                      override fun onResponse(call: Call<Boolean>?, response: Response<Boolean>?) {
                          Log.i("code", response?.code().toString())
                          when {
                              (response?.body() == null) -> {
                                  Toast.makeText(applicationContext, "送信に失敗しました", Toast.LENGTH_SHORT).show()
                              }
                              response.body() == false -> {
                                  Toast.makeText(applicationContext, "送信が拒否されました", Toast.LENGTH_SHORT).show()
                              }
                              else -> {
                                  textArea.setText("", TextView.BufferType.EDITABLE)
                                  Toast.makeText(applicationContext, "送信成功: " + response.body(), Toast.LENGTH_SHORT).show()
                              }
                          }
                      }

                      override fun onFailure(call: Call<Boolean>, t: Throwable) {}
                  })
        }

        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() = updateMessages()
        }, 5000, 5000)
    }

    fun updateMessages() {
        client.getMessages(roomId, token).enqueue(object : Callback<Array<GetMessageResponse>> {
            override fun onResponse(call: Call<Array<GetMessageResponse>>?, response: Response<Array<GetMessageResponse>>?) {
                listAdapter.messages = response?.body() ?: emptyArray()
                listAdapter.notifyDataSetChanged()
            }

            override fun onFailure(call: Call<Array<GetMessageResponse>>?, t: Throwable?) {}
        })
    }
}
