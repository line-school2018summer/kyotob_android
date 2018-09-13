package com.kyotob.client.entities

// チャット一覧を表示するためのデータクラス
data class Room(val roomId: Int,
                val roomName: String,
                val recentMessage: String)

