package com.kyotob.client

import android.support.annotation.IdRes
import android.view.View

// リソースIDを受け取ってオブジェクトを返す処理を簡易的に書くための拡張
fun <T : View> View.bindView(@IdRes id: Int): Lazy<T> = lazy {
    findViewById(id) as T
}