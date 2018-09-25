package com.kyotob.client.register

import android.Manifest
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.kyotob.client.repositories.user.UsersRepository
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import ru.gildor.coroutines.retrofit.awaitResponse
import android.net.Uri
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import com.kyotob.client.*
import com.kyotob.client.R
import com.kyotob.client.chatList.ChatListActivity
import com.kyotob.client.util.ImageDialog
import com.kyotob.client.util.createIconUpload
import com.kyotob.client.util.imageActivityResult
import net.gotev.uploadservice.*

class RegisterActivity : AppCompatActivity() {

    var uri:Uri? = null

    val job = Job()

    val usersRepository = UsersRepository()

    fun showToast(message: String) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_LONG)
        toast.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // NAMESPACE PARAMETER FOR UPLOAD SERVICE
        UploadService.NAMESPACE = "com.kyotob.client"

        val sharedPreferences = getSharedPreferences(USER_DATA_KEY, Context.MODE_PRIVATE)
        val claIntent =  Intent(this, ChatListActivity::class.java)

        //ユーザー登録する
        findViewById<Button>(R.id.register_button_register).setOnClickListener(object: View.OnClickListener{
            override fun onClick(v: View){
                // 二度押し禁止
                v.isEnabled = false

                val name: String = findViewById<EditText>(R.id.id_edittext_register).text.toString()
                val screenName : String = findViewById<EditText>(R.id.username_edittext_register).text.toString()
                val password: String = findViewById<EditText>(R.id.password_edittext_register).text.toString()

                if(name.isEmpty() || screenName.isEmpty() || password.isEmpty()){
                    showToast("Enter ID, UserName and Password")
                    // ボタンを復活
                    v.isEnabled = true
                }
                else {
                    launch(job + UI) {
                        try {
                            var imageUri = "abc.png"

                            //画像
                            if (uri != null) {
                                val part = createIconUpload(uri!!, this@RegisterActivity)
                                val response = usersRepository.uploadIcon(part).awaitResponse()
                                if (response.isSuccessful) {
                                    imageUri = response.body()!!.path
                                    Log.d("image", imageUri)
                                } else {
                                    showToast(response.code().toString())
                                }
                            }
                            val response = usersRepository.register(name, screenName, password, imageUri).awaitResponse()
                            if (response.isSuccessful) {
                                val token = response.body()!!.token
                                val editor = sharedPreferences.edit()
                                editor.putString(USER_NAME_KEY, name)
                                editor.putString(USER_SCREEN_NAME_KEY, screenName)
                                editor.putString(TOKEN_KEY, token)
                                editor.apply()
                                // ボタンクリックを復活
                                v.isEnabled = true
                                // 遷移
                                startActivity(claIntent)
                            } else {
                                // Debug
                                println("error code: " + response.code())
                                // ボタンクリックを復活させる
                                v.isEnabled = true
                            }
                        } catch(t: Throwable) {// Connectionに問題が生じた場合
                        } finally {
                            v.isEnabled = true
                        }

                    }
                }

            }
        })

        //ログイン画面に遷移する
        findViewById<TextView>(R.id.already_have_account_text_view).setOnClickListener{
            finish()
        }

        // ImageViewのインスタンス
        val iconImage  = findViewById<ImageView>(R.id.user_icon)
        // ImageViewの設定
        iconImage.setImageResource(R.drawable.boy)
        // ImageViewをクリック時の挙動
        iconImage.setOnClickListener {
            // 権限の有無を確認する
            if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                // 以前、パーミッションを要求したことがある場合、
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                                Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // パーミッションが断られた場合
                    Toast.makeText(applicationContext, "Please accept STORAGE permission", Toast.LENGTH_LONG).show()
                } else { // 初めて要求する場合、
                    ActivityCompat.requestPermissions(this,
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)
                }
            } else {
                val fragmentManager = supportFragmentManager
                val imageDialog = ImageDialog()
                imageDialog.show(fragmentManager, "image選択")
            }
        }
    }

    // パーミッション要求のコールバック
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
        // 内部フォルダへの書き込み権限
            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // パーミッションが許可された場合
                    Toast.makeText(applicationContext, "Thank you", Toast.LENGTH_LONG).show()
                } else {
                    // パーミッションが断られた場合
                    Toast.makeText(applicationContext, "Please accept STORAGE permission", Toast.LENGTH_LONG).show()
                }
                return
            }
            else -> {
                Toast.makeText(applicationContext, "Other Permission Requested", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        uri = imageActivityResult(requestCode, resultCode, data, this)
        findViewById<ImageView>(R.id.user_icon).setImageURI(uri)
    }

    fun setDefaultIcon() {
        findViewById<ImageView>(R.id.user_icon).setImageResource(R.drawable.boy)
        uri = null
    }
}
