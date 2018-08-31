package com.kyotob.client

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.FloatingActionButton
import com.kyotob.client.adapter.RoomListAdapter
import com.kyotob.client.entities.Room
import android.support.v7.app.AlertDialog
import android.view.WindowManager
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.*

class ChatListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)

        // RoomsViewでカスタマイズされたListViewにデータを入れて、表示させる。その際に、Adapterが緩衝材になる
        // 1, ListViewAdapterのインスタンスをつくる
        val listAdapter = RoomListAdapter(applicationContext)
        // 2, データクラスのリストを用意する
        listAdapter.rooms = listOf(Room(1, "sekiya", "関谷", "こんにちは"),
                Room(2, "taro", "田中", "こんばんわ"))
        // 3, listViewのインスタンスをつくる
        var listView = findViewById(R.id.chats_list) as ListView
        // 4, このクラスのインスタンスをlistViewのadapterに代入することで簡単にlistのitemをデザインできる
        listView.adapter = listAdapter


        // リスト項目タップ時のアクション
        listView.setOnItemClickListener{ adapterView, view, position, id ->
            // タップしたアイテムの情報を取得
            val itemInfo = listAdapter.rooms[position]
            // Debug: トーストを表示
            Toast.makeText(this, "Clicked: ${itemInfo.userScreenName}", Toast.LENGTH_SHORT).show()
            // ChatActivityを表示
            val ChatActivityIntent = Intent(this, ChatActivity::class.java)
            // 遷移先に値を渡す
            ChatActivityIntent.putExtra("ROOM_ID", itemInfo.roomId)
            // 遷移
            startActivity(ChatActivityIntent)
        }

        // FloatingIconのインスタンスを作る
        val addUserButton = findViewById(R.id.add_user_button) as FloatingActionButton

        // FloatingIconをクリックしたときの処理
        addUserButton.setOnClickListener {
            // ダイアログ内のレイアウトにdialog.xmlを利用する
            val inflater = this.layoutInflater.inflate(R.layout.dialog, null, false)

            // ダイアログ内のテキストエリアのインスタンス
            val dialogEditText : EditText = inflater.findViewById(R.id.dialog_edit_text)
            // ダイアログ内のSEARCHボタンのインスタンスを作る
            val searchButton: Button = inflater.findViewById(R.id.search_button)
            // ダイアログ内のdialog_not_found_user_viewのインスタンスを作る
            val dialogNotFoundUser = inflater.findViewById(R.id.dialog_not_found_text_view) as TextView
            // ダイアログ内のfound_userのインスタンスを作る
            val foundUser = inflater.findViewById(R.id.dialog_found_user) as ConstraintLayout

            // テキストエリアを選択状態にする
            dialogEditText.requestFocus()

            // ダイアログの設定
            val dialog = AlertDialog.Builder(this).apply {
            // ダイアログ内のビューを設定する
            setView(inflater)

            }.create()

            // ダイアログ表示と同時にキーボードを表示
            dialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

            // ダイアログを表示する
            dialog.show()

            // SEARCHボタンクリック時の挙動
            searchButton.setOnClickListener{
                // キーボードを非表示
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN

                // 検索時の処理
                if(dialogEditText.text.toString() == "taro") {
                    dialogNotFoundUser.visibility = INVISIBLE
                    foundUser.visibility = VISIBLE
                } else {
                    dialogNotFoundUser.visibility = VISIBLE
                    foundUser.visibility = INVISIBLE
                }
            }
        }
    }
}
