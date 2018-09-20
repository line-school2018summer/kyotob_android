package com.kyotob.client.entities

import com.fasterxml.jackson.annotation.JsonProperty
import java.sql.Timestamp

// チャット一覧を表示するためのデータクラス
//data class Room(val roomId: Int,
//                val roomName: String,
//                val recentMessage: String)

data class Room(
        @JsonProperty("room_id")
        val roomId: Int,
        @JsonProperty("name")
        val roomName: String,
        @JsonProperty("recent_message")
        val recentMessage: String,
        @JsonProperty("created_at")
        val createdAt: Timestamp
)
