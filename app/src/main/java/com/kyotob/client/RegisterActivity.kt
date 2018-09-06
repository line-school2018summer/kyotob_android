package com.kyotob.client

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        //ユーザー登録する
        val registerBtn: Button = findViewById(R.id.registerBtn)
        registerBtn.setOnClickListener(object: View.OnClickListener{
            override fun onClick(v: View){
                //Todo APIにname,screenName,password投げてユーザー登録
            }
        })

    }
}
