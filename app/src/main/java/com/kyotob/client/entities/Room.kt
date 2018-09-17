package com.kyotob.client.entities

import java.sql.Timestamp

// チャット一覧を表示するためのデータクラス
//data class Room(val roomId: Int,
//                val roomName: String,
//                val recentMessage: String)

data class Room(
        val roomId: Int,
        val name: String,
        val recentMessage: String,
        val createdAt: Timestamp
)
