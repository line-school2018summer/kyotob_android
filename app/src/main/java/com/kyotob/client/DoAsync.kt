package com.kyotob.client

import android.os.AsyncTask

// 非同期処理
class DoAsync(private val handler: () -> Unit) : AsyncTask<Void, Void, Void>() {
    override fun doInBackground(vararg params: Void?): Void? {
        handler()
        return null
    }
}