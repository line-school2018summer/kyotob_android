package com.kyotob.client.repositories.user.remote

import kotlinx.coroutines.experimental.Deferred
import retrofit2.Call
import retrofit2.http.*

data class newNamePost(
        val new_screen_name: String
)
interface UserApi {

    @PUT("user/{name}")
    fun putName(
            @Path("name") userName: String,
            @Header("access_token") token: String,
            @Body body: HashMap<String,String>
    ): Call<Void>
}