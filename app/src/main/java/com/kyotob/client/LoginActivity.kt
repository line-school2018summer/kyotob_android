package com.kyotob.client

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //ログインする
        login_button_login.setOnClickListener(object: View.OnClickListener{
            override fun onClick(v: View){
                //Todo APIにname,password投げてログイン
            }
        })
        
        //登録画面に遷移する
        create_new_account_text_view.setOnClickListener{
            val intent =  Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

    }
}
