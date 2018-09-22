package com.kyotob.client

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.widget.Toolbar
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.kyotob.client.entities.GetTimerMessageResponse
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_timer_message_sender.*
import java.sql.Timestamp
import kotlin.concurrent.timer

class TimerMessageViewerDialog: DialogFragment() {
    var msg = emptyArray<GetTimerMessageResponse>()
    // ダイアログを返すメソッド
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // AlertDialogをつくる
        val builder = AlertDialog.Builder(activity)
        // dialog.xmlをつかって、ダイアログをデザインする
        val inflater = activity!!.layoutInflater.inflate(R.layout.dialog_timer_message_viewer, null)

        var index: Int = 0

        // テキストの設定
        val timerText = inflater.findViewById<TextView>(R.id.timer_message_text)
        timerText.text = msg[index].content

        // 画像の設定
        val timerImage = inflater.findViewById<ImageView>(R.id.timer_messsage_image)
        Picasso.get().load(baseUrl + "/image/download/" + msg[index].imageUrl).into(timerImage)
        println(baseUrl + "/image/download/" + msg[index].imageUrl)

        // バーの設定
        val toolbar = inflater.findViewById(R.id.my_toolbar) as? Toolbar
        toolbar?.title = "スペシャルメッセージ"
        toolbar?.setTitleTextColor(Color.WHITE)
        toolbar?.inflateMenu(R.menu.menu_timer_message_view)
        // ボタン押下時に表示を変更する
        toolbar?.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.left -> {
                    Log.d("clicked", "left")
                    if(index-1 > 0) {
                        index--
                        timerText.text = msg[index].content
                        Picasso.get().load(baseUrl + "/image/download/" + msg[index].imageUrl).into(timerImage)
                    }
                    true
                }
                R.id.right -> {
                    Log.d("clicked", "right")
                    if(index+1 < msg.size) {
                        index++
                        timerText.text = msg[index].content
                        Picasso.get().load(baseUrl+ "/image/download/" + msg[index].imageUrl).into(timerImage)
                    }
                    true
                }
                else -> {
                    false
                }
            }
        }

        // builderにビューをセットする
        builder.setView(inflater)

        // bulderを返す
        return builder.create()
    }
}
