package com.kyotob.client.register

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.kyotob.client.R
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_register.*
import com.kyotob.client.login.LoginActivity
import com.kyotob.client.repositories.user.UsersRepository
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import ru.gildor.coroutines.retrofit.awaitResponse

class RegisterActivity : AppCompatActivity() {

    val job = Job()


    val usersRepositry = UsersRepository()

    fun showToast(message: String) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_LONG)
        toast.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val sharedPreferences = getSharedPreferences("userData", Context.MODE_PRIVATE)
        //ユーザー登録する
        findViewById<Button>(R.id.register_button_register).setOnClickListener(object: View.OnClickListener{
            override fun onClick(v: View){

                val name: String = findViewById<EditText>(R.id.id_edittext_register).text.toString()
                val screen_name : String = findViewById<EditText>(R.id.username_edittext_register).text.toString()
                val password: String = findViewById<EditText>(R.id.password_edittext_register).text.toString()
                    launch(CommonPool, parent = job) {
                        try {
                            val response = usersRepositry.register(name, screen_name, password).awaitResponse()
                            if (response.isSuccessful) {
                                val token = response.body()!!.token
                                val editor = sharedPreferences.edit()
                                editor.putString("name",name)
                                editor.putString("screenName",screen_name)
                                editor.putString("accessToken", token)
                                editor.apply()
                                withContext(UI) {
                                    showToast("register successed")
                                }
                            } else {
                                withContext(UI) {
                                    showToast( response.code().toString() )
                                }
                            }
                        } catch (t: Throwable){
                            t.message?.let(::showToast)
                        }
                    }

            }
        })

        //ログイン画面に遷移する
        findViewById<TextView>(R.id.already_have_account_text_view).setOnClickListener{
            val intent =  Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

    }
}
