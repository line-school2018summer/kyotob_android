package com.kyotob.client

import com.kyotob.client.entities.*
import retrofit2.Call
import retrofit2.http.*

interface Client {
    @GET("/user")
    fun getUser(@Query("id") query: String): UserResponse?

    // User検索用
    @GET("/user/search/{user_name}")
    fun searchUser(@Path("user_name") Id : String, @Header("access_token") token: String): Call<SearchUserResponse>

    // Room追加用
    @POST("/room/pair")
    fun makeRoom(@Body body: AddUserRequest, @Header("access_token") token: String): Call<AddUserResponse>

    // チャット一覧画面生成
    @GET("/room")
    fun makeList(@Header("access_token") token: String): Call<List<Room>>

    //
    @GET("/room/{room_id}/messages")
    fun getMessages(@Path("room_id") roomId: Int,
                    @Header("token") token: String): Call<Array<GetMessageResponse>>
}
