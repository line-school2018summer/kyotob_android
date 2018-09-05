package com.kyotob.client

import com.kyotob.client.entities.Message
import com.kyotob.client.entities.User
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface Client {
    @GET("/user")
    fun getUser(@Query("id") query: String): User?

    @GET("/room/{room_id}/messages")
    fun getMessages(@Path("room_id") roomId: Int): Array<Message>?
}
