package com.kyotob.client.entities

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
<<<<<<< HEAD

data class WebSocketMSG(
        val content: String
)
=======
>>>>>>> 5de0f94148a658f319fcc4058574a70bd0318612
