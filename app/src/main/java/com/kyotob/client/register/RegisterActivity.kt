package com.kyotob.client.register

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.kyotob.client.login.LoginActivity
import com.kyotob.client.repositories.user.UsersRepository
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import ru.gildor.coroutines.retrofit.awaitResponse
import android.net.Uri
import com.kyotob.client.*
import com.kyotob.client.R
import com.kyotob.client.util.ImageDialog
import com.kyotob.client.util.createIconUpload
import net.gotev.uploadservice.*
import java.io.File
import java.io.IOException


class RegisterActivity : AppCompatActivity() {

    var uri:Uri? = null


    val job = Job()


    val usersRepositry = UsersRepository()

    fun showToast(message: String) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_LONG)
        toast.show()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // NAMESPACE PARAMETER FOR UPLOADSERVICE
        UploadService.NAMESPACE = "com.kyotob.client"

        val sharedPreferences = getSharedPreferences(USER_DATA_KEY, Context.MODE_PRIVATE)
        val claIntent =  Intent(this, ChatListActivity::class.java)

        //ユーザー登録する
        findViewById<Button>(R.id.register_button_register).setOnClickListener(object: View.OnClickListener{
            override fun onClick(v: View){
                val name: String = findViewById<EditText>(R.id.id_edittext_register).text.toString()
                val screen_name : String = findViewById<EditText>(R.id.username_edittext_register).text.toString()
                val password: String = findViewById<EditText>(R.id.password_edittext_register).text.toString()
                try {
                    launch(CommonPool, parent = job) {

                        var imageUri = "abc.png"

                        //画像
                        if (uri != null) {
                            val part = createIconUpload(uri!!, this@RegisterActivity)
                            val response = usersRepositry.uploadIcon(part).awaitResponse()
                            if (response.isSuccessful) {
                                imageUri = response.body()!!.path
                                Log.d("image",imageUri)
                            } else {
                                showToast(response.code().toString())
                            }
                        }
                        val response = usersRepositry.register(name, screen_name, password, imageUri).awaitResponse()
                        if (response.isSuccessful) {
                            val token = response.body()!!.token
                            val editor = sharedPreferences.edit()
                            editor.putString(USER_NAME_KEY,name)
                            editor.putString(USER_SCREEN_NAME_KEY,screen_name)
                            editor.putString(TOKEN_KEY, token)
                            editor.apply()

                            // 遷移
                            startActivity(claIntent)
                        } else {
                            // Debug
                            println("error code: " + response.code())
                        }
                    }
                } catch (t: Throwable){
                    t.message?.let(::showToast)
                }

            }
        })

        //ログイン画面に遷移する
        findViewById<TextView>(R.id.already_have_account_text_view).setOnClickListener{
            val intent =  Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        // ImageViewのインスタンス
        val iconImage  = findViewById<ImageView>(R.id.user_icon)
        // ImageViewの設定
        iconImage.setImageResource(R.drawable.boy)
        // ImageViewをクリック時の挙動
        iconImage.setOnClickListener {
            val fragmentManager = supportFragmentManager
            val imageDialog = ImageDialog()
            imageDialog.show(fragmentManager, "image選択")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 写真を撮ったときの挙動
        if(requestCode == ImageDialog.TAKE_PICTURE && resultCode == Activity.RESULT_OK) {
            try {
                val imageSharedPreferences = getSharedPreferences(IMAGE_PREFERENCE_KEY, Context.MODE_PRIVATE)
                val file = File(imageSharedPreferences.getString(IMAGE_PATH_KEY,null))
                uri = Uri.fromFile(file)
                findViewById<ImageView>(R.id.user_icon).setImageURI(uri)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        // アルバムから画像を選んだときの挙動
        if(requestCode == ImageDialog.SELECT_PICTURE && resultCode == Activity.RESULT_OK) {
            try {
                uri = data!!.data
                findViewById<ImageView>(R.id.user_icon).setImageURI(uri)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun setDefaultIcon() {
        findViewById<ImageView>(R.id.user_icon).setImageResource(R.drawable.boy)
    }
}