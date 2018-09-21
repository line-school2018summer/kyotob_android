package com.kyotob.client

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.design.widget.TextInputEditText
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.kyotob.client.adapter.MessageListAdapter
//import com.kyotob.client.chatList.ChatListActivity
import com.kyotob.client.entities.GetMessageResponse
import com.kyotob.client.entities.PostMessageRequest
import net.gotev.uploadservice.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {
    // 画像用
    var currentPath: String? = null
    val TAKE_PICTURE = 1
    val SELECT_PICTURE = 2
    var uri: Uri? = null

    private val timer = Timer()
    private lateinit var listAdapter: MessageListAdapter
    private lateinit var client: Client
    private lateinit var token: String
    private var roomId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        title = "チャット"

        // RoomIdを取得する
        val intent = this.intent
        roomId = intent.getIntExtra("ROOM_ID", -1)
        if (roomId == -1) {
            Toast.makeText(applicationContext, "ルームIDの取得に失敗しました", Toast.LENGTH_SHORT).show()
            finish()
        }

        // ListAdapterのインスタンスを作る
        listAdapter = MessageListAdapter(applicationContext)
        // ListViewのインスタンスを作る
        val listView = findViewById<ListView>(R.id.list_view)

        val gson = GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create()

        val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                // レスポンスからオブジェクトへのコンバータファクトリを設定する
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build()

        // クライアントの実装の生成
        client = retrofit.create(Client::class.java)

        val sharedPreferences = getSharedPreferences(USER_DATA_KEY, Context.MODE_PRIVATE)
        token = sharedPreferences.getString(TOKEN_KEY, null) ?: throw Exception("token is null")
        val userName = sharedPreferences.getString(USER_NAME_KEY, null) ?: throw Exception("userName is null")


        client.getMessages(roomId, token).enqueue(object : Callback<Array<GetMessageResponse>> {
            override fun onResponse(call: Call<Array<GetMessageResponse>>?, response: Response<Array<GetMessageResponse>>?) {
                listAdapter.messages = response?.body() ?: emptyArray()
                listView.adapter = listAdapter
            }

            override fun onFailure(call: Call<Array<GetMessageResponse>>?, t: Throwable?) {}
        })

        val submitButton = findViewById<Button>(R.id.submit)
        val textArea = findViewById<TextInputEditText>(R.id.message)

        // when submit button pushed
        submitButton.setOnClickListener {
            if(textArea.text.toString().isNotBlank()) {
                client.sendMessage(roomId, PostMessageRequest(textArea.text.toString(), "string"), token)
                        .enqueue(object : Callback<Boolean> {
                            override fun onResponse(call: Call<Boolean>?, response: Response<Boolean>?) {
                                when {
                                    (response?.body() == null) -> {
                                        Toast.makeText(applicationContext, "送信に失敗しました", Toast.LENGTH_SHORT).show()
                                        Toast.makeText(applicationContext, response!!.code().toString(), Toast.LENGTH_SHORT).show()
                                    }
                                    response.body() == false -> {
                                        Toast.makeText(applicationContext, "送信が拒否されました", Toast.LENGTH_SHORT).show()
                                    }
                                    else -> {
                                        textArea.setText("", TextView.BufferType.EDITABLE)
                                        Toast.makeText(applicationContext, "送信成功: " + response.body(), Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }

                            override fun onFailure(call: Call<Boolean>, t: Throwable) {}
                        })
            } else {
                val items = arrayOf("写真をとる", "写真をえらぶ", "時間差メッセージを送る")
                AlertDialog.Builder(this)
                        .setTitle("オプションメッセージを送信する")
                        .setItems(items, DialogInterface.OnClickListener { _, num ->
                            when(num) {
                                0 -> { dispatchCameraIntent() }
                                1 -> { despatchGallaryIntent() }
                                2 -> {
                                    uri = null
                                    // ChatActivityを表示
                                    val chatActivityIntent = Intent(this, TimerMessageActivity::class.java)
                                    // 遷移先に値を渡す
                                    chatActivityIntent.putExtra("ROOM_ID", roomId)
                                    // 遷移
                                    startActivity(chatActivityIntent)
                                }
                            }
                        })
                        .show()
            }
        }

        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() = updateMessages()
        }, 5000, 5000)
    }

    // メッセージを更新する関数
    fun updateMessages() {
        client.getMessages(roomId, token).enqueue(object : Callback<Array<GetMessageResponse>> {
            override fun onResponse(call: Call<Array<GetMessageResponse>>?, response: Response<Array<GetMessageResponse>>?) {
                listAdapter.messages = response?.body() ?: emptyArray()
                listAdapter.notifyDataSetChanged()
            }

            override fun onFailure(call: Call<Array<GetMessageResponse>>?, t: Throwable?) {}
        })
    }

    // カメラ、アルバムが呼び出されるメソッド
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 写真を撮ったときの挙動
        if(requestCode == TAKE_PICTURE && resultCode == Activity.RESULT_OK) {
            try {
                val file = File(currentPath)
                uri = Uri.fromFile(file)
                // 画像を送信する
                uploadImage()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        // アルバムから画像を選んだときの挙動
        if(requestCode == SELECT_PICTURE && resultCode == Activity.RESULT_OK) {
            try {
                uri = data!!.data
                // 画像を送信する
                uploadImage()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    // 写真をアップロードする関数
    fun uploadImage() {
        Log.d("imagepath", uri!!.path.replace(".*:".toRegex(), "/sdcard/"))
        try {

            MultipartUploadRequest(this, UUID.randomUUID().toString(), "http://192.168.10.139:8080/image/upload")
                    .addFileToUpload(uri!!.path.replace(".*:".toRegex(), "/sdcard/"), "file")
                    .setNotificationConfig(UploadNotificationConfig())
                    .setMaxRetries(2)
                    .setDelegate(DelegeteForUpload { response ->
                        Log.d("画像のURL", baseUrl + "/image/download/" + response)
                        // Todo: 画像urlをメッセージに追加する
    //                        val gson = GsonBuilder()
    //                                //.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
    //                                .create()
    //
    //                        val retrofit = Retrofit.Builder()
    //                                .baseUrl(baseUrl)
    //                                // レスポンスからオブジェクトへのコンバータファクトリを設定する
    //                                .addConverterFactory(GsonConverterFactory.create(gson))
    //                                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
    //                                .build()
    //
    //                        // クライアントの実装の生成
    //                        val client = retrofit.create(Client::class.java)
    //                        client.sendMessage(roomId, PostMessageRequest(userName, textArea.text.toString()), token)
    //                                .enqueue(object : Callback<Boolean> {
    //                                    override fun onResponse(call: Call<Boolean>?, response: Response<Boolean>?) {
    //                                        Log.i("code", response?.code().toString())
    //                                        when {
    //                                            (response?.body() == null) -> {
    //                                                Toast.makeText(applicationContext, "送信に失敗しました", Toast.LENGTH_SHORT).show()
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
    fun despatchGallaryIntent() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select image"), SELECT_PICTURE)
    }

    // カメラ用インテント
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

    // カメラで撮った画像の名前を設定するメソッド
    fun createImage(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageName = timeStamp + "_"
        var storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        var image = File.createTempFile(imageName, ".jpg", storageDir)
        currentPath = image.absolutePath
        return image
    }
}

// 画像アップロード時の挙動を設定する
class DelegeteForUpload(private val handler: (response: String) -> Unit) : UploadStatusDelegate {
    override fun onProgress(context: Context, uploadInfo: UploadInfo) {
        // your code here
    }

    override fun onError(context: Context, uploadInfo: UploadInfo, serverResponse: ServerResponse, exception: Exception) {
        // your code here
    }

    override fun onCompleted(context: Context, uploadInfo: UploadInfo, serverResponse: ServerResponse) {
        handler(serverResponse.bodyAsString)
        // your code here
        // if you have mapped your server response to a POJO, you can easily get it:
        // YourClass obj = new Gson().fromJson(serverResponse.getBodyAsString(), YourClass.class);

    }

    override fun  onCancelled(context: Context, uploadInfo: UploadInfo) {
        // your code here
    }
}