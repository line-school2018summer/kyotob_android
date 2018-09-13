package com.kyotob.client

import com.kyotob.client.entities.*
import retrofit2.Call
import retrofit2.http.*

interface Client {
    @GET("/user")
    fun getUser(@Query("id") query: String): UserResponse?

    // チャット一覧画面生成用テスト用
    @GET("/bins/{id}/")
    fun makeList(@Path("id") Id : String, @Header("access_token") token: String): Call<List<Room>>

    // User検索用
    @GET("/user/search/{user_name}")
    fun searchUser(@Path("user_name") Id : String, @Header("access_token") token: String): Call<SearchUserResponse>

    // Room追加用
    @POST("/room/pair")
    fun makeroom(@Body body: AddUserRequest, @Header("access_token") token: String): Call<AddUserResponse>

//    // チャット一覧画面生成本番用
//    @GET("/room/")
//    fun makeList(@Header("access_token") token: String): Call<List<Room>>
//
//

    @GET("/room/{room_id}/messages")
    fun getMessages(@Path("room_id") roomId: Int,
                    @Header("access_token") token: String): Call<Array<GetMessageResponse>>

    @Headers("Content-Type: application/json")
    @POST("/room/{room_id}/messages")
    fun sendMessage(@Path("room_id") roomId: Int,
                    @Body body: PostMessageRequest,
                    @Header("access_token") token: String): Call<Boolean>
}
