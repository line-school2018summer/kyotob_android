package com.kyotob.client.entities

data class User(val id: Int,
                val name: String,
                val screenName: String,
                val password: String)

// ユーザー検索時のresponse
data class SearchUser(val name: String,
                      val screenName: String)
