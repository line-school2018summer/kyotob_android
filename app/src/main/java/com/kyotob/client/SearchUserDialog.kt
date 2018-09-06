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
import android.widget.Toast
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.kyotob.client.entities.Room
import com.kyotob.client.entities.SearchUser
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
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
        // ユーザー検索欄
        val dialogEditText: EditText = inflater.findViewById(R.id.dialog_edit_text)
        // ユーザー追加ボタン
        val addUserButton: Button = inflater.findViewById(R.id.addUser)

        /* JSON のスネークケースで表現されるフィールド名を、
           Java オブジェクトでキャメルケースに対応させるための設定 */
        val gson = GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create()

        val retrofit = Retrofit.Builder()
//                .baseUrl(getString(R.string.baseUrl))  // PC 側の localhost
                .baseUrl("https://api.myjson.com/")
                // レスポンスからオブジェクトへのコンバータファクトリを設定する
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build()


        // エンターキー押下時の挙動
        dialogEditText.setOnKeyListener { view, keyCode, event ->
            (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN).apply {
                // クライアントの実装の生成
                val client = retrofit.create(Client::class.java)
                // 通信
                client.searchUser("7z4oc", "aaa").enqueue(object : Callback<SearchUser> {
                    // Request成功時に呼ばれる
                    override fun onResponse(call: Call<SearchUser>, response: Response<SearchUser>) {
                        val notFoundView = inflater.findViewById(R.id.dialog_not_found_text_view) as TextView
                        val foundView = inflater.findViewById(R.id.dialog_found_user) as ConstraintLayout
                        val foundText = inflater.findViewById(R.id.dialog_user_name_text_view) as TextView

                        // 通信成功時
                        if(response.isSuccessful) {
                            // TEST
                            // ユーザー表示名の変更
                            foundText.text = response.body().screenName

                            foundView.visibility = View.VISIBLE
                            notFoundView.visibility = View.INVISIBLE
                        }
                        // Bad request
                        else {
                            foundView.visibility = View.INVISIBLE
                            notFoundView.visibility = View.VISIBLE
                        }
                    }

                    // Request失敗時に呼ばれる
                    override fun onFailure(call: Call<SearchUser>?, t: Throwable?) {
                        // Fail to connect Internet access
                        Toast.makeText(context, "Fail to Connect Internet Access", Toast.LENGTH_LONG).show()
                    }
                })
            }
        }

        addUserButton.setOnClickListener {
            // ダイアログを閉じる
            dialog.dismiss()
        }

        // builderにビューをセットする
        builder.setView(inflater)

        // bulderを返す
        return builder.create()
    }
}