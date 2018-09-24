package com.kyotob.client

import android.content.Context
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.kyotob.client.entities.ImageUrl
import net.gotev.uploadservice.ServerResponse
import net.gotev.uploadservice.UploadInfo
import net.gotev.uploadservice.UploadStatusDelegate

// 画像アップロード時の挙動を設定する
class DelegeteForUpload(private val handler: (response: String) -> Unit) : UploadStatusDelegate {
    override fun onProgress(context: Context, uploadInfo: UploadInfo) {
        // your code here
    }

    override fun onError(context: Context, uploadInfo: UploadInfo, serverResponse: ServerResponse, exception: Exception) {
        // your code here
    }

    override fun onCompleted(context: Context, uploadInfo: UploadInfo, serverResponse: ServerResponse) {
        // mapperオブジェクトを作成
        val mapper = jacksonObjectMapper()
        val url = mapper.readValue<ImageUrl>(serverResponse.bodyAsString).path
        handler(url)

    }

    override fun  onCancelled(context: Context, uploadInfo: UploadInfo) {
        // your code here
    }
}