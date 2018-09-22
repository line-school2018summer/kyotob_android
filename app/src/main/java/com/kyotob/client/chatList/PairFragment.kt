package com.kyotob.client.chatList

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.kyotob.client.*
import com.kyotob.client.entities.AddUserRequest
import com.kyotob.client.entities.AddUserResponse
import com.kyotob.client.entities.SearchUserResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

//音波用のインポート
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.EditText
import android.widget.ToggleButton
import com.kyotob.client.R
import java.io.UnsupportedEncodingException
import java.util.*

class PairFragment: Fragment() {

    lateinit var dialogEditText: EditText
    lateinit var addUserButton: Button
    lateinit var notFoundView: TextView
    lateinit var foundView: ConstraintLayout
    lateinit var foundText: TextView
    lateinit var receiveBtn: Button
    lateinit var sendBtn: ToggleButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.dialog_pair, null)
        with(root) {
            // ユーザー検索欄
            dialogEditText = findViewById(R.id.dialog_edit_text)
            // ユーザー追加ボタン
            addUserButton = findViewById(R.id.addUser)
            notFoundView = findViewById(R.id.dialog_not_found_text_view)
            foundView = findViewById(R.id.dialog_found_user)
            foundText = findViewById(R.id.dialog_user_name_text_view)

            //音波通信ボタン
            receiveBtn = findViewById(R.id.soundReceiveBtn)
            sendBtn = findViewById(R.id.soundSendBtn)

        }

        /* JSON のスネークケースで表現されるフィールド名を、
           Java オブジェクトでキャメルケースに対応させるための設定 */
        val gson = GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create()

        val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build()
        // クライアントの実装の生成
        val client = retrofit.create(Client::class.java)

        val sharedPreferences = activity!!.getSharedPreferences(USER_DATA_KEY, Context.MODE_PRIVATE)
        val token = sharedPreferences.getString(TOKEN_KEY, null) ?: throw Exception("token is null")
        val userName = sharedPreferences.getString(USER_NAME_KEY, null) ?: throw Exception("userName is null")

        // エンターキー押下時の挙動
        dialogEditText.setOnKeyListener { _, keyCode, event ->
            (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN).apply {

                // 通信
                client.searchUser(dialogEditText.text.toString(), token).enqueue(object : Callback<SearchUserResponse> {
                    // Request成功時に呼ばれる
                    override fun onResponse(call: Call<SearchUserResponse>, response: Response<SearchUserResponse>) {

                        // 通信成功時
                        if(response.isSuccessful) {
                            // TEST
                            // ユーザー表示名の変更
                            foundText.text = response.body()!!.screenName

                            foundView.visibility = View.VISIBLE
                            notFoundView.visibility = View.INVISIBLE
                        }
                        // Bad request
                        else {
                            foundView.visibility = View.INVISIBLE
                            notFoundView.visibility = View.VISIBLE
                        }
                    }

                    // Request失敗時に呼ばれる
                    override fun onFailure(call: Call<SearchUserResponse>?, t: Throwable?) {
                        // Fail to connect Internet access
                        if (activity != null) {
                                    Toast.makeText(activity?.applicationContext, "Fail to Connect Internet Access", Toast.LENGTH_LONG).show()
                                }
                    }
                })
            }
        }

        addUserButton.setOnClickListener {
            // roomの追加
            client.makeRoom(AddUserRequest(dialogEditText.text.toString()), token).enqueue(object : Callback<AddUserResponse> {
                // Request成功時に呼ばれる
                override fun onResponse(call: Call<AddUserResponse>, response: Response<AddUserResponse>) {
                    // 通信成功時
                    if(response.isSuccessful) {
                        //Toast.makeText(activity?.applicationContext, "Successful", Toast.LENGTH_LONG).show()
                    }
                    // Bad request
                    else {
                        //Toast.makeText(activity?.applicationContext, "Bad request", Toast.LENGTH_LONG).show()
                    }
                    val intent = Intent(activity?.application, ChatListActivity::class.java)
                    startActivity(intent)
                    activity?.finish()
                }

                // Request失敗時に呼ばれる
                override fun onFailure(call: Call<AddUserResponse>?, t: Throwable?) {
                    // Fail to connect Internet access
                    if(this@PairFragment.isVisible) Toast.makeText(context, "Fail to Connect Internet Access", Toast.LENGTH_LONG).show()
                }
            })
            // ダイアログを閉じる
            //dialog.dismiss()
            //activity?.recreate()
        }

        ///音波通信用の実装BELOW///

        ///////
        val SAMPLE_RATE = 44100
        val SEC_PER_SAMPLEPOINT = 1.0f / SAMPLE_RATE
        val AMP = 4000
        val FREQ_BASE = 1000
        val FREQ_STEP = 20
        val FREQ_KEY = FREQ_BASE - 20
        val ELMS_1SEC = SAMPLE_RATE
        val ELMS_100MSEC = SAMPLE_RATE / 10
        val ELMS_MAX = 256

        val PLAY_START = 120
        val PLAY_END = 130

        val mPlayBuf = ShortArray(SAMPLE_RATE)
        val mSignals = Array(ELMS_MAX) { ShortArray(SAMPLE_RATE / 10) }

        val bufferSizeInBytes = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT)

        //再生用
        var mAudioTrack = AudioTrack(AudioManager.STREAM_MUSIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSizeInBytes,
                AudioTrack.MODE_STREAM)

        // サイン波データを生成
        fun createSineWave(buf: ShortArray, freq: Int, amplitude: Int, doClear: Boolean) {
            if (doClear) {
                Arrays.fill(buf, 0.toShort())
            }
            for (i in buf.indices) {
                val currentSec = i * SEC_PER_SAMPLEPOINT // 現在位置の経過秒数
                val `val` = amplitude * Math.sin(2.0 * Math.PI * freq.toDouble() * currentSec.toDouble())
                buf[i] = `val`.toShort()
            }
        }

        // 指定されたバイト値を音声信号に置き換えて再生する
        fun valueToWave(`val`: Byte) {
            mAudioTrack.write(mSignals[`val`.toInt()], 0, ELMS_100MSEC)
        }

        //音波用ハンドラー
        class iHandler(): Handler() {
            override fun handleMessage(msg: Message) {
                when(msg.what){
                    PLAY_END -> {
                        sendBtn.isChecked = false
                    }
                }
            }
        }

        val mHandler = iHandler()

        //音波用Runnable
        class sendRun: Runnable{
            override fun run() {
                // 先頭・終端の目印用信号データ
                createSineWave(mPlayBuf, FREQ_KEY, AMP, true)

                // 256種類の信号データを生成
                for (i in 0 until ELMS_MAX) {
                    createSineWave(mSignals[i], (FREQ_BASE + FREQ_STEP * i).toShort().toInt(), AMP, true)
                }

                //TODO Preferenceから自分のID取得
                val myId = dialogEditText.text.toString()
                val strByte: ByteArray = myId.toByteArray(charset("UTF-8"))

                mAudioTrack.play()
                mAudioTrack.write(mPlayBuf, 0, ELMS_1SEC/2) // 開始
                for (i in strByte.indices) {
                    valueToWave(strByte[i])
                }
                mAudioTrack.write(mPlayBuf, 0, ELMS_1SEC/2) // 終端

                mAudioTrack.stop()
                mAudioTrack.flush()
                mHandler.sendEmptyMessage(PLAY_END)
            }
        }

        //音波で自分のIDを送信
        sendBtn.setOnClickListener{
            if (mAudioTrack.playState != AudioTrack.PLAYSTATE_STOPPED) {
                mAudioTrack.stop()
                mAudioTrack.flush()
            }
            if (sendBtn.isChecked) {
                Thread(sendRun()).start()
            }
        }

        //音波でIDを取得
        receiveBtn.setOnClickListener{
            //TODO 音波受け取ってdialogEditTextにセット

            // 通信
            client.searchUser(dialogEditText.text.toString(), token).enqueue(object : Callback<SearchUserResponse> {
                // Request成功時に呼ばれる
                override fun onResponse(call: Call<SearchUserResponse>, response: Response<SearchUserResponse>) {

                    // 通信成功時
                    if(response.isSuccessful) {
                        // TEST
                        // ユーザー表示名の変更
                        foundText.text = response.body()!!.screenName

                        foundView.visibility = View.VISIBLE
                        notFoundView.visibility = View.INVISIBLE
                    }
                    // Bad request
                    else {
                        foundView.visibility = View.INVISIBLE
                        notFoundView.visibility = View.VISIBLE
                    }
                }

                // Request失敗時に呼ばれる
                override fun onFailure(call: Call<SearchUserResponse>?, t: Throwable?) {
                    // Fail to connect Internet access
                    if (activity != null) {
                        Toast.makeText(activity?.applicationContext, "Fail to Connect Internet Access", Toast.LENGTH_LONG).show()
                    }
                }
            })

        }
        return root
    }
}
