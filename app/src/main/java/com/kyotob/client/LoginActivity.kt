package com.kyotob.client

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //Register画面へ遷移
        val toRegisterBtn: Button = findViewById(R.id.toRegisterBtn)
        toRegisterBtn.setOnClickListener(object: View.OnClickListener{
            override fun onClick(v: View){
                //Register画面へ遷移
                val registerActivityIntent = Intent(this@LoginActivity, RegisterActivity::class.java)
                startActivity(registerActivityIntent)
            }
        })

        //Loginする
        val loginBtn: Button = findViewById(R.id.loginBtn)
        loginBtn.setOnClickListener(object: View.OnClickListener{
            override fun onClick(v: View){
                //Todo APIにnameとpassword投げてログイン
            }
        })
    }
}
