/*
 * Copyright 2017, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kyotob.client.repositories.user.remote

import com.kyotob.client.repositories.remoteUtil.CommonInterceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class UsersRemoteDataSource
 {
     val retrofit = Retrofit.Builder()
             .baseUrl("http://localhost:8080/")
             .client(OkHttpClient.Builder().addInterceptor(CommonInterceptor()).build())
             .addConverterFactory(GsonConverterFactory.create())
             .build()
     val api = retrofit.create(UserApi::class.java)

    fun updateUser(name: String, accessToken: String, newName: String): Call<Void> {
        return api.putName(name, accessToken, hashMapOf("new_screen_name" to newName))
    }
}