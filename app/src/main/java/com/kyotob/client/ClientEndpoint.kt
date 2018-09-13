package com.kyotob.client

import android.app.Activity
import android.os.Handler
import android.widget.Toast
import javax.websocket.*

@javax.websocket.ClientEndpoint
class ClientEndpoint() {

    // Socket通信を開始するときに呼び出される
    @OnOpen
    fun onOpen(session: Session, config: EndpointConfig) {
        println("client-[open] " + session)
    }

    // Message受信時に呼び出される
    @OnMessage
    fun onMessage(message: String, session: Session) {
        println("client-[message][$message] $session")
    }

    // Socket通信を終了するときに呼び出される
    @OnClose
    fun onClose(session: Session) {
        println("client-[close] $session")
    }

    // ERRORのログを取る
    @OnError
    fun onError(session: Session?, t: Throwable?) {
        println("client-[error] ${t?.message} $session")
    }
}
