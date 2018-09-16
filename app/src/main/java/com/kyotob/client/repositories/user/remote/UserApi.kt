package com.kyotob.client.repositories.user.remote

import kotlinx.coroutines.experimental.Deferred
import retrofit2.Call
import retrofit2.http.*
import com.kyotob.client.entities.LoginResponse

data class newNamePost(
        val new_screen_name: String
)
interface UserApi {

    @PUT("user/{name}")
    fun putName(
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
}