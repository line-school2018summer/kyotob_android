package com.kyotob.client

import com.kyotob.client.entities.Room
import com.kyotob.client.entities.SearchUser
import com.kyotob.client.entities.User
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface Client {
    @GET("/user")
    fun getUser(@Query("id") query: String): User?

    @GET("/bins/{id}/")
    fun makeList(@Path("id") Id : String, @Header("token") token: String): Call<List<Room>>


    @GET("/bins/{id}/")
    fun searchUser(@Path("id") Id : String, @Header("token") token: String): Call<SearchUser>
}
