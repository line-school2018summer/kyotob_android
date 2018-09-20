package com.kyotob.client.entities

import com.fasterxml.jackson.annotation.JsonProperty
import java.sql.Timestamp

data class UserResponse(val id: Int,
                val name: String,
                val screenName: String,
                val password: String)

// ユーザー検索時のresponse
data class SearchUserResponse(val name: String,
                      val screenName: String,
                      val imageUrl: String)

// ユーザー登録時のrequest
data class AddUserRequest(
        @JsonProperty("user_name")
        val userName: String
)

data class AddUserResponse(val roomId: String,
                           val friendScreenName: String)

data class WebSocketMessage(
        val createdAt: Timestamp,
        val screenName: String,
        val roomId: String,
        val content: String
)

data class LoginResponse(val screenName: String,
                         val token: String)

data class FriendItem(
        val friendScreenName: String,
        val friendName: String
)

data class FriendListResponse(
        val friendList: List<FriendItem>
)
