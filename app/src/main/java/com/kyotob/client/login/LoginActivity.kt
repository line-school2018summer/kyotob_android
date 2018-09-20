package com.kyotob.client.login

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.kyotob.client.*
import com.kyotob.client.register.RegisterActivity
import com.kyotob.client.repositories.user.UsersRepository
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import ru.gildor.coroutines.retrofit.awaitResponse

class LoginActivity : AppCompatActivity() {

    private val usersRepositry = UsersRepository()


    private val job = Job()

    private lateinit var sharedPreferences: SharedPreferences

    fun showToast(message: String) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_LONG)
        toast.show()
    }

    var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sharedPreferences = getSharedPreferences(USER_DATA_KEY, Context.MODE_PRIVATE)
        sharedPreferences.registerOnSharedPreferenceChangeListener { sp, _ ->
            println(sp.getString(TOKEN_KEY, null) ?: throw Exception("token is null"))
            println(sp.getString(USER_NAME_KEY, null) ?: throw Exception("token is null"))
            // 遷移
            if (count == 1) {
                finish()
            }
            count++
        }

        //ログインする
        findViewById<Button>(R.id.login_button_login).setOnClickListener {
            val name: String = findViewById<EditText>(R.id.id_edittext_login).text.toString()
            val password: String = findViewById<EditText>(R.id.password_edittext_login).text.toString()

            launch(job + UI) {
                val response = withContext(CommonPool) {
                    usersRepositry.login(name, password).awaitResponse()
                }

                if (response.isSuccessful) {
                    val token = response.body()!!.token
                    register(token, name)
                    startActivity(Intent(this@LoginActivity, ChatListActivity::class.java))

                } else {
                    // Debug
                    println("error code: " + response.code())
                }
            }
        }

        //登録画面に遷移する
        findViewById<TextView>(R.id.create_new_account_text_view).setOnClickListener{
            val intent =  Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

    }

     private fun register(token: String, name: String) {
         println("Register")
         val editor = sharedPreferences.edit()
         editor.putString(TOKEN_KEY, token) // tokenをセット
         editor.putString(USER_NAME_KEY, name) // userIdをセット
         editor.commit()
     }

}
