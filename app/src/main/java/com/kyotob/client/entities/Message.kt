package com.kyotob.client.entities

import com.fasterxml.jackson.annotation.JsonProperty
import java.sql.Timestamp

data class Message(val id: Int,
                   val senderId: Int,
                   val roomId: Int,
                   val content: String,
                   val created: Timestamp)

data class GetMessageResponse(
        @JsonProperty("created_at") val createdAt: Timestamp,
        @JsonProperty("user_name") val userName: String,
        @JsonProperty("user_screen_name") val userScreenName: String,
        @JsonProperty("content") val content: String,
        @JsonProperty("content_type") val contentType: String
)

data class PostMessageRequest(val content: String,
                              val contentType: String)

// 画像のURLパース用
data class ImageUrl(val path: String)

// TimerMessageの取得時に返すResponseの項目
data class GetTimerMessageResponse(
        @JsonProperty("created_at") val createdAt: Timestamp,
        @JsonProperty("user_name") val userName: String,
        @JsonProperty("user_screen_name") val userScreenName: String,
        @JsonProperty("content") val content: String,
        @JsonProperty("image_url") val imageUrl: String
)

// TimerMessage送信時のRequestの項目
data class SendTimerMessageRequest(
        @JsonProperty("content") val content: String,
        @JsonProperty("image_url") val imageUrl: String,
        val timer: Int
)