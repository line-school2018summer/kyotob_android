package com.kyotob.client

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.app.AlertDialog
import android.content.Context
import android.support.constraint.ConstraintLayout
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.kyotob.client.entities.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


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
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build()
        // クライアントの実装の生成
        val client = retrofit.create(Client::class.java)

        val sharedPreferences = activity!!.getSharedPreferences(USERDATAKEY, Context.MODE_PRIVATE)
        val token = sharedPreferences.getString(TOKENKEY, null) ?: throw Exception("token is null")
        val userName = sharedPreferences.getString(USERNAMEKEY, null) ?: throw Exception("userName is null")

        // エンターキー押下時の挙動
        dialogEditText.setOnKeyListener { _, keyCode, event ->
            (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN).apply {

                // 通信
                client.searchUser(dialogEditText.text.toString(), token).enqueue(object : Callback<SearchUserResponse> {
                    // Request成功時に呼ばれる
                    override fun onResponse(call: Call<SearchUserResponse>, response: Response<SearchUserResponse>) {
                        val notFoundView = inflater.findViewById(R.id.dialog_not_found_text_view) as TextView
                        val foundView = inflater.findViewById(R.id.dialog_found_user) as ConstraintLayout
                        val foundText = inflater.findViewById(R.id.dialog_user_name_text_view) as TextView

                        // 通信成功時
                        if(response.isSuccessful) {
                            // TEST
                            // ユーザー表示名の変更
                            foundText.text = response.body()!!.screenName

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
                    override fun onFailure(call: Call<SearchUserResponse>?, t: Throwable?) {
                        // Fail to connect Internet access
                        Toast.makeText(context, "Fail to Connect Internet Access", Toast.LENGTH_LONG).show()
                    }
                })
            }
        }

        addUserButton.setOnClickListener {
            // roomの追加
            client.makeRoom(AddUserRequest(userName , dialogEditText.text.toString()), token).enqueue(object : Callback<AddUserResponse> {
                // Request成功時に呼ばれる
                override fun onResponse(call: Call<AddUserResponse>, response: Response<AddUserResponse>) {
                    // 通信成功時
                    if(response.isSuccessful) {
//                        Toast.makeText(context, "Successful", Toast.LENGTH_LONG).show()
                    }
                    // Bad request
                    else {
//                        Toast.makeText(context, "Bad request", Toast.LENGTH_LONG).show()
                    }
                }

                // Request失敗時に呼ばれる
                override fun onFailure(call: Call<AddUserResponse>?, t: Throwable?) {
                    // Fail to connect Internet access
//                    Toast.makeText(context, "Fail to Connect Internet Access", Toast.LENGTH_LONG).show()
                }
            })
            // ダイアログを閉じる
            dialog.dismiss()
        }

        // builderにビューをセットする
        builder.setView(inflater)

        // builderを返す
        return builder.create()
    }
}
