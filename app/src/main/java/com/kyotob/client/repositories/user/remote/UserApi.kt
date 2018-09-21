package com.kyotob.client.repositories.user.remote

import com.kyotob.client.entities.FriendItem
import kotlinx.coroutines.experimental.Deferred
import retrofit2.Call
import retrofit2.http.*
import com.kyotob.client.entities.LoginResponse
import com.kyotob.client.repositories.user.IconUploadResponce
import okhttp3.MultipartBody

data class newNamePost(
        val new_screen_name: String
)


data class PostGroupRoomRequest(
        val room_name: String,
        val user_name_list: List<HashMap<String, String>>
)
interface UserApi {

    @PUT("user/{name}")
    fun putUserData(
            @Path("name") userName: String,
            @Header("access_token") token: String,
            @Body body: HashMap<String, String>
    ): Call<Unit>

    @POST("user")
    fun postUser(
            @Body body: HashMap<String, String>
    ): Call<LoginResponse>

    @POST("user/login")
    fun loginUser(
            @Body body: HashMap<String, String>
    ): Call<LoginResponse>

    @POST("room")
    fun postGroupRoom(
            @Header("access_token") token: String,
            @Body body: PostGroupRoomRequest
    ): Call<Unit>

    @GET("user/{name}/friends")
    fun getFriendList(
            @Path("name") name: String,
            @Header("access_token") token: String
    ): Call<List<FriendItem>>

    @Multipart
    @POST("image/upload")
    fun uploadIcon(@Part file: MultipartBody.Part): Call<IconUploadResponce>
}

