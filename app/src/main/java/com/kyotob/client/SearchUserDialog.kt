package com.kyotob.client

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.app.AlertDialog
import android.os.AsyncTask
import android.support.constraint.ConstraintLayout
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL


// Dialogの諸々の設定ををするクラス
class SearchUserDialog : DialogFragment() {

    // ダイアログを返すメソッド
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // AlertDialogをつくる
        val builder = AlertDialog.Builder(activity)
        // dialog.xmlをつかって、ダイアログをデザインする
        val inflater = activity!!.layoutInflater.inflate(R.layout.dialog, null)

        val dialogEditText: EditText = inflater.findViewById(R.id.dialog_edit_text)

        val addUserButton: Button = inflater.findViewById(R.id.addUser)

        // エンターキー押下時の挙動
        dialogEditText.setOnKeyListener { view, keyCode, event ->
            (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN).apply {
                // URLをセット
                val url = "https://api.myjson.com/bins/c8dng"
                // 非同期で通信し、responseを使って、画面描画する
                val ac = AsyncConnecter()
                ac.iView = inflater
                ac.execute(url)
            }
        }

        addUserButton.setOnClickListener {
            dialog.dismiss()
        }

        // builderにビューをセットする
        builder.setView(inflater)

        // bulderを返す
        return builder.create()
    }


    // 非同期で通信するための内部クラス"url"を引数にとる
    inner class AsyncConnecter : AsyncTask<String, String, String>() {
        val requestMethod = "GET"
        val accessToken = "f7d30ccc-6fd0-4c53-9cc7-ae9ad4846b69"
        var iView = View(context)

        // AsyncTaskが呼び出されると、まずdoInBackgroundがbackgroundで動く
        override fun doInBackground(vararg url: String?): String {
            // responseをためておく変数
            var text: String
            // 接続用の設定
            val connection = URL(url[0]).openConnection() as HttpURLConnection
            // 接続用の設定をする
            // Request Method
            connection.setRequestMethod(requestMethod)
            // Request Header
            connection.addRequestProperty("access_token", accessToken)

            // 接続を試みる
            try {
                // 通信開始
                connection.connect()

                // responseを変数(text)に入れる
                text = connection.inputStream.use { it.reader().use { reader -> reader.readText() } }
            } catch (e: Exception) { // 例外時の処理。要検討
                text = ""
            } finally {
                // 通信が成功しても、失敗しても最後に、切断する
                connection.disconnect()
            }
            // 通信結果をdoInBackgroundに返す
            return text
        }

        // doInBackgroundが終了するとonPostExecuteがUIスレッドから呼び出される
        override fun onPostExecute(result: String?) { // 引数(result)はdoInBackgroundの戻り値(return text)
            super.onPostExecute(result)

            showResult(result)
        }

        // 通信結果のJsonをパースして、UIに反映させる
        private fun showResult(jsonString: String?) {
        val notFoundView = iView.findViewById(R.id.dialog_not_found_text_view) as TextView
        val foundView = iView.findViewById(R.id.dialog_found_user) as ConstraintLayout
        val foundText = iView.findViewById(R.id.dialog_user_name_text_view) as TextView

        Log.d("text", jsonString)

        if (jsonString != "") {

            // 文字列 -> JSONに
            val jsonArray = JSONArray(jsonString)

            // JSONをパースして、President型に形成し、Arrayに追加する
            for( i in 0..(jsonArray.length()-1)) {
                val jsonObject = jsonArray.getJSONObject(i)
                foundText.text = jsonObject.getString("screen_name")
            }



            // TEST
            foundView.visibility = View.VISIBLE
            notFoundView.visibility = View.INVISIBLE
        } else { // エラー時(Tokenのミスマッチ)の処理。要検討！！
            foundView.visibility = View.INVISIBLE
            notFoundView.visibility = View.VISIBLE
        }


        }
    }



}