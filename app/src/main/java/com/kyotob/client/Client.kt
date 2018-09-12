package com.kyotob.client

import com.kyotob.client.entities.GetMessageResponse
import com.kyotob.client.entities.User
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.Call
import retrofit2.http.Header

interface Client {
    @GET("/user")
    fun getUser(@Query("id") query: String): User?

    @GET("/room/{room_id}/messages")
    fun getMessages(@Path("room_id") roomId: Int, @Header("token") token: String): Call<Array<GetMessageResponse>>
}
