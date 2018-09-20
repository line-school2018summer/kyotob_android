package com.kyotob.client.entities

import java.sql.Timestamp

data class UserResponse(val id: Int,
                val name: String,
                val screenName: String,
                val password: String)

// ユーザー検索時のresponse
data class SearchUserResponse(val name: String,
                      val screenName: String)

// ユーザー登録時のrequest
data class AddUserRequest(val myUserName: String,
                   val friendUserName: String)

data class AddUserResponse(val roomId: String,
                           val friendScreenName: String)

data class WebSocketMessage(
        val createdAt: Timestamp,
        val screenName: String,
        val roomId: String,
        val content: String
)

data class LoginResponse(val token: String)
