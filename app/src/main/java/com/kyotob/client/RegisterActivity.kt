package com.kyotob.client

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        //ユーザー登録する
        register_button_register.setOnClickListener(object: View.OnClickListener{
            override fun onClick(v: View){
                //Todo APIにname,screen_name,password投げてユーザー登録
            }
        })

        //ログイン画面に遷移する
        already_have_account_text_view.setOnClickListener{
            val intent =  Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

    }
}
