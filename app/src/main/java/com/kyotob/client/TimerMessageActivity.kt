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
import com.kyotob.client.entities.SendTimerMessageRequest
import com.squareup.picasso.Picasso
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
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
    // 画像URL
    private lateinit var imageURL: String
    // ImageView
    private lateinit var image: ImageView
    // 時間
    private var time: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer_message_sender)
        // 画像用
        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID
        UploadService.NAMESPACE = "com.kyotob.client"
        // RoomIdを取得する
        val intent = this.intent
        roomId = intent.getIntExtra("ROOM_ID", -1)
        // 画像URLを初期化
        imageURL = "def.png"

        image = findViewById(R.id.timer_send_image_view)
        image.setImageResource(R.drawable.def)

        // ImageView押下時の挙動
        findViewById<ImageView>(R.id.timer_send_image_view).setOnClickListener {
            val items = arrayOf("写真をとる", "写真をえらぶ", "デフォルトに戻す")
                AlertDialog.Builder(this)
                        .setTitle("画像を選択する")
                        .setItems(items, DialogInterface.OnClickListener { _, num ->
                            when(num) {
                                0 -> { dispatchCameraIntent() }
                                1 -> { despatchGallaryIntent() }
                                2 -> {
                                    imageURL = "def.png"
                                    image.setImageResource(R.drawable.def)
                                }
                            }
                        })
                        .show()
        }

        // 送信ボタン押下時の挙動
        findViewById<Button>(R.id.send_button).setOnClickListener {
            // テキストエリアからメッセージを取得
            val message:String = findViewById<EditText>(R.id.message_edit_text).text.toString()
            // テキストエリアから経過時間を取得
            val time:Int = findViewById<EditText>(R.id.time_edit_text).text.toString().toInt()

            // 空文字か、0以下,画像が含まれていない場合
            if(message.isEmpty() || time <= 0 || imageURL.isEmpty()) {
                // 何もしない
            } else {
                // Tokenを取得
                val sharedPreferences = getSharedPreferences(USER_DATA_KEY, Context.MODE_PRIVATE)
                val token = sharedPreferences.getString(TOKEN_KEY, null)
                        ?: throw Exception("token is null")

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
                client.sendTimerMessage(roomId, SendTimerMessageRequest(message, imageURL, time), token)
                        .enqueue(object : Callback<Boolean> {
                            override fun onResponse(call: Call<Boolean>?, response: Response<Boolean>?) {
                                when {
                                    (response?.body() == null) -> {
                                        Toasty.error(applicationContext, "送信に失敗しました", Toast.LENGTH_SHORT, true).show()
                                    }
                                    response.body() == false -> {
                                        Toasty.warning(applicationContext, "送信が拒否されました", Toast.LENGTH_SHORT, true).show()
                                    }
                                    else -> {
                                        // Todo: アニメーションつけたい
                                        Toasty.success(applicationContext, "メッセージを${time}時間後に送信します", Toast.LENGTH_SHORT, true).show()
                                    }
                                }
                            }

                            override fun onFailure(call: Call<Boolean>?, t: Throwable?) {
                                // 通信失敗時の処理
                            }
                        })
                // 戻る
                finish()
            }
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
                        Log.d("IMAGE URL", response)
                        // 画像を再設定
                        imageURL = response
                        // UIスレッドで画像を再設定
                        launch(UI) {
                            Picasso.get().load("$baseUrl/image/download/$imageURL").into(image)
                        }
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
