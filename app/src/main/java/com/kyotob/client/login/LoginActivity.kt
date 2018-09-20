package com.kyotob.client.login

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.kyotob.client.*
import kotlinx.android.synthetic.main.activity_login.*
import com.kyotob.client.register.RegisterActivity
import com.kyotob.client.repositories.user.UsersRepository
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import ru.gildor.coroutines.retrofit.awaitResponse
import java.net.ConnectException

class LoginActivity : AppCompatActivity() {

    private val usersRepositry = UsersRepository()


    private val job = Job()


    fun showToast(message: String) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_LONG)
        toast.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val sharedPreferences = getSharedPreferences(USER_DATA_KEY, Context.MODE_PRIVATE)

        //ログインする
        findViewById<Button>(R.id.login_button_login).setOnClickListener {
            val name: String = findViewById<EditText>(R.id.id_edittext_login).text.toString()
            val password: String = findViewById<EditText>(R.id.password_edittext_login).text.toString()
            launch(CommonPool, parent = job) {
                try {
                    val response = usersRepositry.login(name, password).awaitResponse()
                    if (response.isSuccessful) {
                        val token = response.body()!!.token
                        val screenName = response.body()!!.screenName
                        val editor = sharedPreferences.edit()
                        editor.putString(USER_NAME_KEY, name)
                        editor.putString(TOKEN_KEY, token)
                        editor.putString(USER_SCREEN_NAME_KEY, screenName)
                        editor.apply()
                        withContext(UI) {
                            showToast("login successed")
                        }
                    } else {
                        withContext(UI) {
                            showToast( response.code().toString())
                        }
                    }
                } catch (exception: Throwable) {
                    withContext(UI) {
                        showToast(exception.message!!)
                    }
                }
            }
        }

        //登録画面に遷移する
        findViewById<TextView>(R.id.create_new_account_text_view).setOnClickListener{
            val intent =  Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

    }

}
