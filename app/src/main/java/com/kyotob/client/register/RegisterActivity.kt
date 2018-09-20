package com.kyotob.client.register

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.kyotob.client.R
import android.util.Log
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.activity_register.*
import com.kyotob.client.login.LoginActivity
import com.kyotob.client.repositories.user.UsersRepository
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import ru.gildor.coroutines.retrofit.awaitResponse
import android.content.DialogInterface
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class RegisterActivity : AppCompatActivity() {

    // 画像用
    var currentPath: String? = null
    val TAKE_PICTURE = 1
    val SELECT_PICTURE = 2

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

        // ImageViewのインスタンス
        val iconImage  = findViewById<ImageView>(R.id.user_icon)
        // ImageViewの設定
        iconImage.setImageResource(R.drawable.boy)
        // ImageViewをクリック時の挙動
        iconImage.setOnClickListener {
            val items = arrayOf("写真をとる", "写真をえらぶ", "デフォルトに戻す")
            AlertDialog.Builder(this)
                    .setTitle("ユーザーアイコンの設定")
                    .setItems(items, DialogInterface.OnClickListener { _, num ->
                        when(num) {
                            0 -> { dispatchCameraIntent() }
                            1 -> { despatchGallaryIntent() }
                            2 -> {findViewById<ImageView>(R.id.user_icon).setImageResource(R.drawable.boy)}
                        }
                    })
                    .show()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 写真を撮ったときの挙動
        if(requestCode == TAKE_PICTURE && resultCode == Activity.RESULT_OK) {
            try {
                val file = File(currentPath)
                val uri = Uri.fromFile(file)
                findViewById<ImageView>(R.id.user_icon).setImageURI(uri)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        // アルバムから画像を選んだときの挙動
        if(requestCode == SELECT_PICTURE && resultCode == Activity.RESULT_OK) {
            try {
                val uri = data!!.data
                findViewById<ImageView>(R.id.user_icon).setImageURI(uri)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    // GallaryActivity
    fun despatchGallaryIntent() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select image"), SELECT_PICTURE)
    }

    // CameraActivity
    fun dispatchCameraIntent() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if(intent.resolveActivity(packageManager) != null) {
            var photoFile: File? = null
            try {
                photoFile = createImage()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            if(photoFile != null) {
                var photoUri = FileProvider.getUriForFile(this,
                        "com.kyotob.client.fileprovider", photoFile)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(intent, TAKE_PICTURE)
            }
        }
    }

    fun createImage(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageName = timeStamp + "_"
        var storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        var image = File.createTempFile(imageName, ".jpg", storageDir)
        currentPath = image.absolutePath
        return image
    }
}
