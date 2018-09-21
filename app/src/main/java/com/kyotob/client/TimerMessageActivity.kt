package com.kyotob.client

import android.app.Activity
import android.content.*
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.support.v7.app.*
import android.util.Log
import android.widget.*
import com.google.gson.*
import com.kyotob.client.entities.PostMessageRequest
import net.gotev.uploadservice.*
import retrofit2.*
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class TimerMessageActivity : AppCompatActivity() {
    // 画像用
    var currentPath: String? = null
    var uri: Uri? = null
    private var roomId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer_message_sender)
        // 画像用
        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID
        UploadService.NAMESPACE = "com.kyotob.client"
        // RoomIdを取得する
        val intent = this.intent
        roomId = intent.getIntExtra("ROOM_ID", -1)

        val imageView = findViewById<ImageView>(R.id.imageView)

        // when submit button pushed
        imageView.setOnClickListener {
            val items = arrayOf("写真をとる", "写真をえらぶ", "デフォルトに戻す")
                AlertDialog.Builder(this)
                        .setTitle("画像を選択する")
                        .setItems(items, DialogInterface.OnClickListener { _, num ->
                            when(num) {
                                0 -> { dispatchCameraIntent() }
                                1 -> { despatchGallaryIntent() }
                                2 -> {
                                    uri = null
                                }
                            }
                        })
                        .show()

        }

        findViewById<Button>(R.id.send_button).setOnClickListener {
            // 戻る
            finish()
        }
    }
    // カメラ、アルバムが呼び出されるメソッド
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 写真を撮ったときの挙動
        if(requestCode == TAKE_PICTURE && resultCode == Activity.RESULT_OK) {
            try {
                val file = File(currentPath)
                uri = Uri.fromFile(file)
                Log.d("URI", uri.toString())
                // 画像を送信する
                uploadImage()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        // アルバムから画像を選んだときの挙動
        if(requestCode == SELECT_PICTURE && resultCode == Activity.RESULT_OK) {
            try {
                val file = File(UriToFile().getPathFromUri(applicationContext, data!!.data))
                uri = Uri.fromFile(file)
                Log.d("URI", uri.toString())
                // 画像を送信する
                uploadImage()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    // 写真をアップロードする関数
    private fun uploadImage() {
        Log.d("URI", uri!!.toString())
        try {
            MultipartUploadRequest(this, UUID.randomUUID().toString(), baseUrl + "image/upload")
                    .addFileToUpload(uri!!.path.replace(".*:".toRegex(), "/sdcard/"), "file")
                    .setNotificationConfig(UploadNotificationConfig())
                    .setMaxRetries(2)
                    .setDelegate(DelegeteForUpload { response ->
                        // Tokenを取得
                        val sharedPreferences = getSharedPreferences(USER_DATA_KEY, Context.MODE_PRIVATE)
                        val token = sharedPreferences.getString(TOKEN_KEY, null) ?: throw Exception("token is null")

                        // retrofit通信用
                        val gson = GsonBuilder()
                                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                                .create()
                        val retrofit = Retrofit.Builder()
                                .baseUrl(baseUrl)
                                // レスポンスからオブジェクトへのコンバータファクトリを設定する
                                .addConverterFactory(GsonConverterFactory.create(gson))
                                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                                .build()
                        // メッセージ送信
                        val client = retrofit.create(Client::class.java)
                        Log.d("画像のURL", response)
//                        client.sendMessage(roomId, PostMessageRequest(response, "image"), token)
//                                .enqueue(object : Callback<Boolean> {
//                                    override fun onResponse(call: Call<Boolean>?, response: Response<Boolean>?) {
//                                        when {
//                                            (response?.body() == null) -> {
//                                                Toast.makeText(applicationContext, "送信に失敗しました", Toast.LENGTH_SHORT).show()
//                                                Toast.makeText(applicationContext, response!!.code().toString(), Toast.LENGTH_SHORT).show()
//                                            }
//                                            response.body() == false -> {
//                                                Toast.makeText(applicationContext, "送信が拒否されました", Toast.LENGTH_SHORT).show()
//                                            }
//                                            else -> {
//                                                Toast.makeText(applicationContext, "送信成功: " + response.body(), Toast.LENGTH_SHORT).show()
//                                            }
//                                        }
//                                    }
//
//                                    override fun onFailure(call: Call<Boolean>, t: Throwable) {}
//                                })
                    })
                    .startUpload()
            Log.d("finishflag", "aaa")
        } catch (e: Exception) {
            Log.e("AndroidUploadService", e.toString())
        }
    }

    // アルバム用インテント
    private fun despatchGallaryIntent() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select image"), SELECT_PICTURE)
    }

    // カメラ用インテント
    private fun dispatchCameraIntent() {
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

    // カメラで撮った画像の名前を設定するメソッド
    private fun createImage(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageName = timeStamp + "_"
        var storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        var image = File.createTempFile(imageName, ".jpg", storageDir)
        currentPath = image.absolutePath
        return image
    }
}
