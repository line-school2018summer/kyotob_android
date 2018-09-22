package com.kyotob.client

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.view.Menu
import android.view.MenuItem
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
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
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.kyotob.client.chatList.Dialog
import com.kyotob.client.database.RoomDatabaseHelper
import com.kyotob.client.database.RoomsMidokuModel
import com.kyotob.client.setting.SettingActivity
// WebSocket用
import java.net.URI
import java.sql.Timestamp
import javax.websocket.*

// サーバーからWebSocketのプロトコルを使ってメッセージを送るときに使うメッセージ
data class WebSocketMessage(
        val createdAt: Timestamp,
        val screenName: String,
        val roomId: Int,
        val content: String
)

class ChatListActivity : AppCompatActivity() {
    lateinit var listAdapter: RoomListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)

        // Request for the permission to access device's microphone
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.RECORD_AUDIO), 1)
        }

        // RoomsViewでカスタマイズされたListViewにデータを入れて、表示させる。その際に、Adapterが緩衝材になる
        // 1, ListViewAdapterのインスタンスをつくる
        listAdapter = RoomListAdapter(applicationContext)
        // 2, listViewのインスタンスをつくる
        var listView = findViewById<ListView>(R.id.chats_list)
        // 3, このクラスのインスタンスをlistViewのadapterに代入することで簡単にlistのitemをデザインできる
        listView.adapter = listAdapter


        // リスト項目タップ時のアクション
        listView.setOnItemClickListener{ _, _, position, _ ->
            // タップしたアイテムの情報を取得
            val itemInfo = listAdapter.rooms[position]

            // ---------- SQLITE ----------------
            val roomDatabaseHelper = RoomDatabaseHelper(this) // インスタンス
            roomDatabaseHelper.updateData(itemInfo.roomId, 0) // データの挿入
            // ----------------------------------
            // ChatActivityを表示
            val chatActivityIntent = Intent(this, ChatActivity::class.java)
            // 遷移先に値を渡す
            chatActivityIntent.putExtra("ROOM_ID", itemInfo.roomId)
            // 遷移
            startActivityForResult(chatActivityIntent, 200)
        }

        // FloatingIconのインスタンスを作る
        val addUserButton = findViewById<FloatingActionButton>(R.id.add_user_button)
        // FloatingIconをクリックしたときの処理
        addUserButton.setOnClickListener {
            // SearchUserDialogのインスタンスをつくる
            val dialog = Dialog()
            // Dialogを表示
            dialog.show(supportFragmentManager, "dialog")
        }

        // 通信 -> パース -> 表示の更新
        updateChatList()

        // WebSocket用の通信を非同期(AsyncTask)で実行
        DoAsync {
            val sharedPreferences = getSharedPreferences(USER_DATA_KEY, Context.MODE_PRIVATE)
            val name = sharedPreferences.getString(USER_NAME_KEY, null) ?: throw Exception("name is null")

            // 初期化のため WebSocket コンテナのオブジェクトを取得する
            val container = ContainerProvider.getWebSocketContainer()
            // サーバー・エンドポイントの URI
            val uri = URI.create("wss://$baseIP/$name") // 要変更
            try {
                // サーバー・エンドポイントとのセッションを確立する
                container.connectToServer(WebSocketEndPoint { msg ->
                    // jsonパース
                    val mapper = jacksonObjectMapper()
                    val webSocketMessage = mapper.readValue<WebSocketMessage>(msg)

                    // ---------- SQLITE ----------------
                    val roomDatabaseHelper = RoomDatabaseHelper(this) // インスタンス
                    // データを検索
                    val midokuNum = roomDatabaseHelper.searchData(webSocketMessage.roomId)
                    if (midokuNum == -1) { // 新規Roomの場合
                        val midokuModel = RoomsMidokuModel(webSocketMessage.roomId, 0) // データ
                        roomDatabaseHelper.inserData(midokuModel) // データの挿入
                    } else {// 既存のRoomの場合
                        roomDatabaseHelper.updateData(webSocketMessage.roomId, midokuNum + 1) // データの挿入
                    }
                    // ----------------------------------

                    // Messageを受信すると、chatListの表示を更新する
                    updateChatList()
                }, uri)
            } catch (e: Exception) {
                // Fail to connect Internet access
                println("Fail to Connect Websocket Access")
            }

        }.execute()
        // ----------------------------------------
    }

    // ChatActivityから戻ってきたときに実行される
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 画面を更新する
        updateChatList()
    }

    // AppBarにボタンを追加
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.setting_icon, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // Settingボタン押下時の挙動
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId
        if (id == R.id.setting_item) {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }


    // 通信結果のJsonをパースして、UIに反映させる
    fun updateChatList() {
        val sharedPreferences = getSharedPreferences(USER_DATA_KEY, Context.MODE_PRIVATE)
        val token = sharedPreferences.getString(TOKEN_KEY, null)

        /* JSON のスネークケースで表現されるフィールド名を、
Java オブジェクトでキャメルケースに対応させるための設定 */
        val gson = GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create()

        val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                // レスポンスからオブジェクトへのコンバータファクトリを設定する
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build()

        // クライアントの実装の生成
        val client = retrofit.create(Client::class.java)

        // 通信
        client.makeList(token).enqueue(object : Callback<List<Room>> {
            // Request成功時に呼ばれる
            override fun onResponse(call: Call<List<Room>>, response: Response<List<Room>>) {
                // 通信成功時
                if (response.isSuccessful) {
                    // 一覧を更新する
                    listAdapter.rooms = response.body()!!
                    listAdapter.notifyDataSetChanged()
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