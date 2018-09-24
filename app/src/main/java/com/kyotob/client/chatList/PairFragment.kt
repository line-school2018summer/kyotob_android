package com.kyotob.client.chatList

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.content.Context
import android.content.SharedPreferences
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

import org.jtransforms.fft.DoubleFFT_1D
import android.media.MediaRecorder
import android.widget.Button
import android.widget.TextView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.dialog_pair.*

class PairFragment: Fragment() {

    lateinit var dialogEditText: EditText
    lateinit var addUserButton: Button
    lateinit var notFoundView: TextView
    lateinit var foundView: ConstraintLayout
    lateinit var foundText: TextView
    lateinit var receiveBtn: Button
    lateinit var sendBtn: ToggleButton
    lateinit var iconImage: ImageView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.dialog_pair, null)
        with(root) {
            // ユーザー検索欄
            dialogEditText = findViewById(R.id.dialog_edit_text)
            // ユーザー追加ボタン
            addUserButton = findViewById<Button>(R.id.addUser)
            notFoundView = findViewById(R.id.dialog_not_found_text_view)
            foundView = findViewById(R.id.dialog_found_user)
            foundText = findViewById<EditText>(R.id.dialog_user_name_text_view)

            //音波通信ボタン
            receiveBtn = findViewById(R.id.soundReceiveBtn)
            sendBtn = findViewById(R.id.soundSendBtn)
            iconImage = findViewById(R.id.dialog_user_image_view)

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


                            // ユーザーアイコンを設定
                            Picasso.get().load(baseUrl + "/image/download/" + response.body()!!.imageUrl).into(iconImage)

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

        ///送受信に共通するパラメータ///
        val SAMPLE_RATE = 44100
        val SEC_PER_SAMPLEPOINT = 1.0f / SAMPLE_RATE
        val AMP = 4000
        val FREQ_BASE = 1000
        val FREQ_STEP = 20
        val FREQ_HEAD = FREQ_BASE - 10
        val FREQ_TAIL = FREQ_BASE - 20
        val ELMS_1SEC = SAMPLE_RATE
        val ELMS_100MSEC = SAMPLE_RATE / 10
        val ELMS_MAX = 256

        val RECORD_START = 100
        val RECORD_END = 110
        val DATA_RECV = 120
        val PLAY_START = 130
        val PLAY_END = 140

        val bufferSizeInBytes = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT)

        //音波用ハンドラー
        class iHandler(): Handler() {
            override fun handleMessage(msg: Message) {
                when(msg.what){
                    PLAY_END -> {
                        sendBtn.isChecked = false
                    }
                    RECORD_START ->{
                        soundReceiveBtn.text = "STOP"
                    }
                    RECORD_END ->{
                        soundReceiveBtn.text = "RECEIVE"
                    }
                    DATA_RECV -> {
                        val ch = byteArrayOf(msg.arg1.toByte())
                        try {
                            // 受信データを表示
                            var s = ch.toString(Charsets.UTF_8)
                            s = dialogEditText.text.toString() + s
                            dialogEditText.setText(s)
                        } catch (e: UnsupportedEncodingException) {
                        }

                    }
                }
            }
        }
        val mHandler = iHandler()

        ///送信側の実装///
        val mPlayBufHEAD = ShortArray(SAMPLE_RATE)
        val mPlayBufTAIL = ShortArray(SAMPLE_RATE)
        val mSignals = Array(ELMS_MAX) { ShortArray(SAMPLE_RATE / 10) }
        val preferences = this.getActivity()!!.getSharedPreferences(USER_DATA_KEY, Context.MODE_PRIVATE)
        val myId = preferences.getString(USER_NAME_KEY, null) ?: throw Exception("name is null")

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

        //送信用Runnable
        class sendRun: Runnable{
            override fun run() {
                // 先頭の目印用信号データ
                createSineWave(mPlayBufHEAD, FREQ_HEAD, AMP, true)
                // 終端の目印用信号データ
                createSineWave(mPlayBufTAIL, FREQ_TAIL, AMP, true)

                // 256種類の信号データを生成
                for (i in 0 until ELMS_MAX) {
                    createSineWave(mSignals[i], (FREQ_BASE + FREQ_STEP * i).toShort().toInt(), AMP, true)
                }
                
                Log.d("SND","myId: $myId")
                val strByte: ByteArray = myId.toByteArray(charset("UTF-8"))

                mAudioTrack.play()
                mAudioTrack.write(mPlayBufHEAD, 0, ELMS_1SEC/2) // 開始
                for (i in strByte.indices) {
                    valueToWave(strByte[i])
                }
                mAudioTrack.write(mPlayBufTAIL, 0, ELMS_1SEC/2) // 終端

                mAudioTrack.stop()
                mAudioTrack.flush()
                mHandler.sendEmptyMessage(PLAY_END)
            }
        }


        ///受信側の実装///
        //パラメーター
        val THRESHOLD_SILENCE: Short = 0x00ff
        val FREQ_MAX = FREQ_BASE + 255 * FREQ_STEP
        val UNITSIZE = SAMPLE_RATE / 10 // 100msec分

        var mInRecording = false
        var mStop = false

        val mBufferSizeInShort: Int = bufferSizeInBytes / 2
        // 集音用バッファ
        val mRecordBuf = ShortArray(mBufferSizeInShort)

        // FFT 処理用
        val mTestBuf = ShortArray(UNITSIZE)
        val mFFTSize = UNITSIZE
        val mFFT = DoubleFFT_1D(mFFTSize.toLong())
        val mFFTBuffer = DoubleArray(mFFTSize)

        var mAudioRecord = AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSizeInBytes)

        fun doFFT(data: ShortArray): Int {
            for (i in 0 until mFFTSize) {
                mFFTBuffer[i] = data[i].toDouble()
            }
            // FFT 実行
            mFFT.realForward(mFFTBuffer)

            // 処理結果の複素数配列からピーク周波数成分の要素番号を得る
            var maxAmp = 0.0
            var index = 0
            for (i in 0 until mFFTSize / 2) {
                val a = mFFTBuffer[i * 2] // 実部
                val b = mFFTBuffer[i * 2 + 1] // 虚部
                // a+ib の絶対値 √ a^2 + b^2 = r が振幅値
                val r = Math.sqrt(a * a + b * b)
                if (r > maxAmp) {
                    maxAmp = r
                    index = i
                }
            }
            return index * SAMPLE_RATE / mFFTSize
        }

        //受信用Runnable
        class receiveRun: Runnable{
            override fun run() {
                var dataCount = 0
                var bSilence = false
                mHandler.sendEmptyMessage(RECORD_START)
                // 集音開始
                mAudioRecord.startRecording()
                loop@while (mInRecording && !mStop) {
                    // 音声データ読み込み
                    mAudioRecord.read(mRecordBuf, 0, mBufferSizeInShort)
                    bSilence = true
                    for (i in 0 until mBufferSizeInShort) {
                        val s = mRecordBuf[i]
                        if (s > THRESHOLD_SILENCE) {
                            bSilence = false
                        }
                    }
                    if (bSilence) { // 静寂
                        dataCount = 0
                        continue
                    }
                    var copyLength = 0
                    // データを mTestBuf へ順次アペンド
                    if (dataCount < mTestBuf.size) {
                        // mTestBuf の残領域に応じてコピーするサイズを決定
                        val remain = mTestBuf.size - dataCount
                        if (remain > mBufferSizeInShort) {
                            copyLength = mBufferSizeInShort
                        } else {
                            copyLength = remain
                        }
                        System.arraycopy(mRecordBuf, 0, mTestBuf, dataCount, copyLength)
                        dataCount += copyLength
                    }
                    if (dataCount >= mTestBuf.size) {
                        // 100ms 分溜まったら FFT にかける
                        var freq = doFFT(mTestBuf)

                        /*Log.d("RCV","$freq")

                        //終端を検知したら終了
                        if(FREQ_TAIL - 5 < freq && freq < FREQ_TAIL + 5){
                            Log.d("RCV","end0")
                            break@loop
                        }*/

                        // 待ってた範囲の周波数かチェック
                        if (freq >= FREQ_BASE && freq <= FREQ_MAX) {
                            val `val` = (freq - FREQ_BASE) / FREQ_STEP
                            if (`val` >= 0 && `val` <= 255) {
                                val msg = Message()
                                msg.what = DATA_RECV
                                msg.arg1 = `val`
                                mHandler.sendMessage(msg)
                            } else {
                                freq = -1
                            }
                        } else {
                            freq = -1
                        }

                        dataCount = 0
                        if (freq == -1) {
                            continue
                        }
                        // mRecordBuf の途中までを mTestBuf へコピーして FFT した場合は
                        // mRecordBuf の残データを mTestBuf 先頭へコピーした上で継続
                        if (copyLength < mBufferSizeInShort) {
                            val startPos = copyLength
                            copyLength = mBufferSizeInShort - copyLength
                            System.arraycopy(mRecordBuf, startPos, mTestBuf, 0, copyLength)
                            dataCount += copyLength
                        }
                    }
                }
                // 集音終了
                mAudioRecord.stop()
                mHandler.sendEmptyMessage(RECORD_END)
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
            // 集音開始 or 終了
            if (!mInRecording) {
                //テキストボックスをリセット
                dialogEditText.setText("")
                mInRecording = true
                Thread(receiveRun()).start()
            } else {
                mInRecording = false
            }
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
