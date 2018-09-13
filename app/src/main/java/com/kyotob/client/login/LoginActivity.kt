package com.kyotob.client.login

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.kyotob.client.R
import android.view.View
import android.widget.Button
import android.widget.EditText
import kotlinx.android.synthetic.main.activity_login.*
import com.kyotob.client.register.RegisterActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //ログインする
        login_button_login.setOnClickListener(object: View.OnClickListener{
            override fun onClick(v: View){
                val name: String = findViewById<EditText>(R.id.id_edittext_login).text.toString()
                val password: String = findViewById<EditText>(R.id.password_edittext_login).text.toString()

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
