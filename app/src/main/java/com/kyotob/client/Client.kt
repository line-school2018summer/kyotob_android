package com.kyotob.client

import com.kyotob.client.entities.User
import retrofit2.http.GET
import retrofit2.http.Query

interface Client {
    @GET("/user")
    fun getUser(@Query("id") query: String): User?
}
