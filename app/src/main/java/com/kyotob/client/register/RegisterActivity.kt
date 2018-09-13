package com.kyotob.client.register

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.kyotob.client.R
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_register.*
import com.kyotob.client.login.LoginActivity

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        //ユーザー登録する
        register_button_register.setOnClickListener(object: View.OnClickListener{
            override fun onClick(v: View){

                val name: String = findViewById<EditText>(R.id.id_edittext_register).text.toString()
                val screen_name : String = findViewById<EditText>(R.id.username_edittext_register).text.toString()
                val password: String = findViewById<EditText>(R.id.password_edittext_register).text.toString()

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
