package com.kyotob.client

import android.content.Intent
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import com.kyotob.client.adapter.RoomListAdapter
import com.kyotob.client.entities.Room
import android.widget.*
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import android.widget.Toast
// WebSocket用
//import javax.websocket.ContainerProvider


class ChatListActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)

        // RoomsViewでカスタマイズされたListViewにデータを入れて、表示させる。その際に、Adapterが緩衝材になる
        // 1, ListViewAdapterのインスタンスをつくる
        val listAdapter = RoomListAdapter(applicationContext)
        // 2, listViewのインスタンスをつくる
        var listView = findViewById<ListView>(R.id.chats_list)
        // 3, このクラスのインスタンスをlistViewのadapterに代入することで簡単にlistのitemをデザインできる
        listView.adapter = listAdapter

        // リスト項目タップ時のアクション
        listView.setOnItemClickListener{ _, _, position, _ ->
            // タップしたアイテムの情報を取得
            val itemInfo = listAdapter.rooms[position]
            // Debug: トーストを表示
            Toast.makeText(this, "Clicked: ${itemInfo.userScreenName}", Toast.LENGTH_SHORT).show()
            // ChatActivityを表示
            val chatActivityIntent = Intent(this, ChatActivity::class.java)
            // 遷移先に値を渡す
            chatActivityIntent.putExtra("ROOM_ID", itemInfo.roomId)
            // 遷移
            startActivity(chatActivityIntent)
        }

        // FloatingIconのインスタンスを作る
        val addUserButton = findViewById<FloatingActionButton>(R.id.add_user_button)
        // FloatingIconをクリックしたときの処理
        addUserButton.setOnClickListener {
            // SearchUserDialogのインスタンスをつくる
            val dialog = SearchUserDialog()
            // Dialogを表示
            dialog.show(supportFragmentManager, "dialog")
        }

        // 通信 -> パース -> 表示の更新
        updateChatList(listAdapter)

//        // WebSocket用の通信を非同期(AsyncTask)で実行
//        DoAsync {
//            // 初期化のため WebSocket コンテナのオブジェクトを取得する
//            val container = ContainerProvider.getWebSocketContainer()
//            // サーバー・エンドポイントの URI
//            val uri = URI.create("ws://10.129.173.46:8181/chat/abc") // LocalHostはだめ
//            // サーバー・エンドポイントとのセッションを確立する
//            container.connectToServer(ClientEndpoint(), uri)
//        }.execute()
//        // ----------------------------------------
    }

    // 通信結果のJsonをパースして、UIに反映させる
    fun updateChatList(chatListAdapter: RoomListAdapter) {
        /* JSON のスネークケースで表現されるフィールド名を、
Java オブジェクトでキャメルケースに対応させるための設定 */
        val gson = GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create()

        val retrofit = Retrofit.Builder()
//                .baseUrl(getString(R.string.baseUrl))  // PC 側の localhost
                .baseUrl("https://api.myjson.com/") // テスト用
                // レスポンスからオブジェクトへのコンバータファクトリを設定する
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build()

        // クライアントの実装の生成
        val client = retrofit.create(Client::class.java)

        // 通信
//        client.makeList("foo").enqueue(object : Callback<List<Room>> { // 本番用
        client.makeList("ic9jo", "aaa").enqueue(object : Callback<List<Room>> { // テスト用
            // Request成功時に呼ばれる
            override fun onResponse(call: Call<List<Room>>, response: Response<List<Room>>) {
                // 通信成功時
                if (response.isSuccessful) {
                    // 一覧を更新する
                    chatListAdapter.rooms = response.body()!!
                    chatListAdapter.notifyDataSetChanged()
                } else { // Bad request
                    Toast.makeText(applicationContext, "Bad Request", Toast.LENGTH_LONG).show()
                }
            }

            // Request失敗時に呼ばれる
            override fun onFailure(call: Call<List<Room>>?, t: Throwable?) {
                // Fail to connect Internet access
                Toast.makeText(applicationContext, "Fail to Connect Internet Access", Toast.LENGTH_LONG).show()
            }
        })

    }
}

// 非同期処理
class DoAsync(private val handler: () -> Unit) : AsyncTask<Void, Void, Void>() {
    override fun doInBackground(vararg params: Void?): Void? {
        handler()
        return null
    }
}