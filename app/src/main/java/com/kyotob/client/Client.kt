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
                    @Header("access_token") token: String): Call<Array<GetMessageResponse>>

    @Headers("Content-Type: application/json")
    @POST("/room/{room_id}/messages")
    fun sendMessage(@Path("room_id") roomId: Int,
                    @Body body: PostMessageRequest,
                    @Header("access_token") token: String): Call<Boolean>

    // 時間差メッセージの受信
    @GET("/room/{room_id}/messages/timer")
    fun getTimerMessages(@Path("room_id") roomId: Int,
                    @Header("access_token") token: String): Call<Array<GetTimerMessageResponse>>

    // 時間差メッセージの送信
    @Headers("Content-Type: application/json")
    @POST("/room/{room_id}/messages/timer")
    fun sendTimerMessage(@Path("room_id") roomId: Int,
                    @Body body: SendTimerMessageRequest,
                    @Header("access_token") token: String): Call<Boolean>

}
