package com.kyotob.client

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity

import android.os.Bundle
import android.os.Handler
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.DialogFragment
import android.view.View
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
import com.kyotob.client.database.RoomDatabaseHelper
import com.kyotob.client.database.RoomsMidokuModel
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

            // ---------- SQLITE ----------------
            val roomDatabaseHelper = RoomDatabaseHelper(this) // インスタンス
            roomDatabaseHelper.updateData(itemInfo.roomId, 0) // データの挿入
            // ----------------------------------
            updateChatList(listAdapter) // 画面の更新
            // Debug: トーストを表示
            Toast.makeText(this, "Clicked: ${itemInfo.roomInfo.roomName}", Toast.LENGTH_SHORT).show()
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

        // WebSocket用の通信を非同期(AsyncTask)で実行
        DoAsync {
            val sharedPreferences = getSharedPreferences(USERDATAKEY, Context.MODE_PRIVATE)
            val name = sharedPreferences.getString("name", null) ?: throw Exception("name is null")

            // 初期化のため WebSocket コンテナのオブジェクトを取得する
            val container = ContainerProvider.getWebSocketContainer()
            // サーバー・エンドポイントの URI
            val uri = URI.create("ws://" + baseWSSIP + name) // 適宜変更
            try {
                // サーバー・エンドポイントとのセッションを確立する
                container.connectToServer(WebSocketEndPoint { msg ->
                    println("message" + msg)
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
                        roomDatabaseHelper.updateData(webSocketMessage.roomId, midokuNum+1) // データの挿入
                    }
                    // ----------------------------------

                    // Messageを受信すると、chatListの表示を更新する
                    updateChatList(listAdapter)
                }, uri)
            } catch (e: Exception) {
                // Fail to connect Internet access
                println("Fail to Connect Websocket Access")
            }

        }.execute()
        // ----------------------------------------
    }


    // 通信結果のJsonをパースして、UIに反映させる
    fun updateChatList(chatListAdapter: RoomListAdapter) {
        val sharedPreferences = getSharedPreferences(USERDATAKEY, Context.MODE_PRIVATE)
        val token = sharedPreferences.getString(TOKENKEY, null) ?: throw Exception("token is null")

        /* JSON のスネークケースで表現されるフィールド名を、
Java オブジェクトでキャメルケースに対応させるための設定 */
        val gson = GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create()

        val retrofit = Retrofit.Builder()
                .baseUrl("http://" + baseIP)
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

// WebSocket
@javax.websocket.ClientEndpoint
class WebSocketEndPoint(private val handler: (msg: String) -> Unit) {

    // Socket通信を開始するときに呼び出される
    @OnOpen
    fun onOpen(session: Session, config: EndpointConfig) {
        println("client-[open] " + session)
    }

    // Message受信時に呼び出される
    @OnMessage
    fun onMessage(message: String, session: Session) {
        println("client-[message][$message] $session")
        if(message != "WebSocket通信を開始します。") { // 最初のメッセージは無視する
            handler(message)
        }
    }

    // Socket通信を終了するときに呼び出される
    @OnClose
    fun onClose(session: Session) {
        println("client-[close] $session")
    }

    // ERRORのログを取る
    @OnError
    fun onError(session: Session?, t: Throwable?) {
        println("client-[error] ${t?.message} $session")
    }
}

class dialogDissmissHandler(private val handler: () -> Unit) : DialogInterface {
    override fun cancel() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
    override fun dismiss() {
        handler()
    }
}