package com.kyotob.client.entities

import com.fasterxml.jackson.annotation.JsonProperty
import java.sql.Timestamp

data class Room(
        val roomId: Int,
        val roomInfo: RoomInfo,
        val recentMessage: String,
        val createdAt: Timestamp
)
// ChatListに表示するルーム情報(名前と画像URL)
data class RoomInfo(
        @JsonProperty("room_name")
        val roomName: String,
        @JsonProperty("image_url")
        val imageUrl: String
)