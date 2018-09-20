package com.kyotob.client.entities

import com.fasterxml.jackson.annotation.JsonProperty
import java.sql.Timestamp

data class Room(
        @JsonProperty("room_id")
        val roomId: Int,
        @JsonProperty("room_info")
        val roomInfo: RoomInfo,
        @JsonProperty("recent_message")
        val recentMessage: String,
        @JsonProperty("created_at")
        val createdAt: Timestamp
)
// ChatListに表示するルーム情報(名前と画像URL)
data class RoomInfo(
        @JsonProperty("room_name")
        val roomName: String,
        @JsonProperty("image_url")
        val imageUrl: String
)