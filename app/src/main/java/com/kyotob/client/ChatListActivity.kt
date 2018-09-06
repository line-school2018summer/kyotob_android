package com.kyotob.client

import android.content.Intent
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import com.kyotob.client.adapter.RoomListAdapter
import com.kyotob.client.entities.Room
import android.widget.*
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL


class ChatListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)

        // URLをセット
        val url = "https://api.myjson.com/bins/ic9jo"
        // 非同期で通信し、responseを使って、画面描画する
        AsyncConnecter().execute(url)


        // FloatingIconのインスタンスを作る
        val addUserButton = findViewById<FloatingActionButton>(R.id.add_user_button)
        // FloatingIconをクリックしたときの処理
        addUserButton.setOnClickListener {
            // SearchUserDialogのインスタンスをつくる
            val dialog = SearchUserDialog()
            // Dialogを表示
            dialog.show(supportFragmentManager, "dialog")
        }
    }


    // 非同期で通信するための内部クラス"url"を引数にとる
    inner class AsyncConnecter : AsyncTask<String, String, String>() {
        private val requestMethod = "GET"
        private val accessToken = "f7d30ccc-6fd0-4c53-9cc7-ae9ad4846b69"

        // AsyncTaskが呼び出されると、まずdoInBackgroundがbackgroundで動く
        override fun doInBackground(vararg url: String?): String {
            // responseをためておく変数
            var text: String
            // 接続用の設定
            val connection = URL(url[0]).openConnection() as HttpURLConnection
            // 接続用の設定をする
            // Request Method
            connection.requestMethod = requestMethod
            // Request Header
            connection.addRequestProperty("access_token", accessToken)

            // 接続を試みる
            try {
                // 通信開始
                connection.connect()

                // responseを変数(text)に入れる
                text = connection.inputStream.use { it.reader().use{reader -> reader.readText()} }
            }
            catch (e: Exception) { // 例外時の処理。要検討
                text = ""
            }
            finally {
                // 通信が成功しても、失敗しても最後に、切断する
                connection.disconnect()
            }
            // 通信結果をdoInBackgroundに返す
            return text
        }

        // doInBackgroundが終了するとonPostExecuteがUIスレッドから呼び出される
        override fun onPostExecute(result: String?) { // 引数(result)はdoInBackgroundの戻り値(return text)
            super.onPostExecute(result)

            makeList(result)
        }
    }
    // 通信結果のJsonをパースして、UIに反映させる
    private fun makeList(jsonString: String?) {
        // ListAdapterに値を渡すために、President型のArrayListをつくる
        val list = ArrayList<Room>()
        if (jsonString != "") {
            // 文字列 -> JSONに
            val jsonArray = JSONArray(jsonString)

            // JSONをパースして、President型に形成し、Arrayに追加する
            for( i in 0..(jsonArray.length()-1)) {
                val jsonObject = jsonArray.getJSONObject(i)
                // 型(data class President)に当てはめる
                list.add(Room(
                        jsonObject.getInt("room_id"),
                        jsonObject.getString("user_name"),
                        jsonObject.getString("user_screen_name"),
                        jsonObject.getString("recent_message")
                ))
            }
        } else { // エラー時(Tokenのミスマッチ)の処理。要検討！！
            list.add(Room(0, "", "No User", ""))
        }


        // RoomsViewでカスタマイズされたListViewにデータを入れて、表示させる。その際に、Adapterが緩衝材になる
        // 1, ListViewAdapterのインスタンスをつくる
        val listAdapter = RoomListAdapter(applicationContext)
        // 2, データクラスのリストを用意する
        listAdapter.rooms = list
        // 3, listViewのインスタンスをつくる
        var listView = findViewById<ListView>(R.id.chats_list)
        // 4, このクラスのインスタンスをlistViewのadapterに代入することで簡単にlistのitemをデザインできる
        listView.adapter = listAdapter

        // リスト項目タップ時のアクション
        listView.setOnItemClickListener{ adapterView, view, position, id ->
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
    }
}
