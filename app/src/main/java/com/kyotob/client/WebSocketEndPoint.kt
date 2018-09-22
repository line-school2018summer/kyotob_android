package com.kyotob.client

import java.util.concurrent.CopyOnWriteArraySet
import javax.websocket.*

// WebSocket
@javax.websocket.ClientEndpoint
class WebSocketEndPoint(private val handler: (msg: String) -> Unit) {
    companion object {
        // sessionを記録しておくためのArray\
        val sessions = CopyOnWriteArraySet<Session>()
    }

    // Socket通信を開始するときに呼び出される
    @OnOpen
    fun onOpen(session: Session, config: EndpointConfig) {
        println("client-[open] " + session)
    }

    // Message受信時に呼び出される
    @OnMessage
    fun onMessage(message: String, session: Session) {
        println("client-[message][$message] $session")
        if(message != "WebSocket通信を開始します。") { // 最初のメッセージは無視する
            handler(message)
        }
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