package com.kyotob.client

import android.Manifest
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.design.widget.TextInputEditText
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.KeyEvent
import android.widget.*
import com.google.gson.*
import com.kyotob.client.adapter.MessageListAdapter
import com.kyotob.client.database.RoomDatabaseHelper
import com.kyotob.client.entities.GetMessageResponse
import com.kyotob.client.entities.GetTimerMessageResponse
import com.kyotob.client.entities.PostMessageRequest
import net.gotev.uploadservice.*
import retrofit2.*
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.*
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*
import javax.websocket.ContainerProvider

class ChatActivity : AppCompatActivity() {
    // 画像用
    private var currentPath: String? = null
    private var uri: Uri? = null

    private val timer = Timer()
    private lateinit var listAdapter: MessageListAdapter
    private lateinit var client: Client
    private lateinit var token: String
    private var roomId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        title = "チャット"

        // 画像用
        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID
        UploadService.NAMESPACE = "com.kyotob.client"

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

        // 送信ボタン押下
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
                    val items = arrayOf("写真をとる", "写真をえらぶ", "時間差メッセージを送る")
                    AlertDialog.Builder(this)
                            .setTitle("オプションメッセージを送信する")
                            .setItems(items, DialogInterface.OnClickListener { _, num ->
                                when (num) {
                                    0 -> {
                                        dispatchCameraIntent()
                                    }
                                    1 -> {
                                        despatchGallaryIntent()
                                    }
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
        }

//        timer.scheduleAtFixedRate(object : TimerTask() {
//            override fun run() = updateMessages()
//        }, 5000, 5000)
        updateMessages()
        // WebSocket用の通信を非同期(AsyncTask)で実行
        DoAsync {
            val sharedPreferences = getSharedPreferences(USER_DATA_KEY, Context.MODE_PRIVATE)
            val name = sharedPreferences.getString(USER_NAME_KEY, null) ?: throw Exception("name is null")

            // 初期化のため WebSocket コンテナのオブジェクトを取得する
            val container = ContainerProvider.getWebSocketContainer()
            // サーバー・エンドポイントの URI
            val uri = URI.create("wss://$baseIP/$name") // 要変更
            try {
                // サーバー・エンドポイントとのセッションを確立する
                container.connectToServer(WebSocketEndPoint {
                    // Messageを受信すると、chatListの表示を更新する
                    updateMessages()
                }, uri)
            } catch (e: Exception) {
                // Fail to connect Internet access
                println("Fail to Connect Websocket Access")
            }
        }.execute()
        // ----------------------------------------
    }

    // メッセージを更新
    fun updateMessages() {
        client.getMessages(roomId, token).enqueue(object : Callback<Array<GetMessageResponse>> {
            override fun onResponse(call: Call<Array<GetMessageResponse>>?, response: Response<Array<GetMessageResponse>>?) {
                Log.d("responseBody", response?.body().toString())
                listAdapter.messages = response?.body() ?: emptyArray()
                listAdapter.notifyDataSetChanged()
            }

            override fun onFailure(call: Call<Array<GetMessageResponse>>?, t: Throwable?) {}
        })

        // 時間差送信メッセージを取得する関数
        client.getTimerMessages(roomId, token).enqueue(object : Callback<Array<GetTimerMessageResponse>> {
            override fun onResponse(call: Call<Array<GetTimerMessageResponse>>?, response: Response<Array<GetTimerMessageResponse>>?) {
                if(response!!.isSuccessful) {
                    if (response?.body()!!.contentEquals(emptyArray())) {
                        println("空")
                    } else {
                        // メッセージを取得
                        val tmpMessage: Array<GetTimerMessageResponse> = response.body()!!
                        // SearchUserDialogのインスタンスをつくる
                        val dialog = TimerMessageViewerDialog()
                        dialog.msg = response.body()!!
                        // Dialogを表示
                        dialog.show(supportFragmentManager, "dialog")
                    }
                } else {
                    Toast.makeText(this@ChatActivity, response.code().toString(), Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<Array<GetTimerMessageResponse>>?, t: Throwable?) {
                // 通信失敗時の処理
            }
        })
    }
    // パーミッション要求のコールバック
    override fun onRequestPermissionsResult(requestCode: Int,
                                                   permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            // 内部フォルダへの書き込み権限
            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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

    // 戻るボタン押下時の挙動
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // 遷移元のの未読数を0にする
        // ---------- SQLITE ----------------
        val roomDatabaseHelper = RoomDatabaseHelper(this) // インスタンス
        roomDatabaseHelper.updateData(roomId, 0) // データの挿入
        // ----------------------------------
        finish()
        return true
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
                        Log.d("画像のURL", response)
                        client.sendMessage(roomId, PostMessageRequest(response, "image"), token)
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
                                                Toast.makeText(applicationContext, "送信成功: " + response.body(), Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }

                                    override fun onFailure(call: Call<Boolean>, t: Throwable) {}
                                })
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
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(imageName, ".jpg", storageDir)
        currentPath = image.absolutePath
        return image
    }
}