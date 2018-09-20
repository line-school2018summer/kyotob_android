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

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.kyotob.client.baseUrl
import com.kyotob.client.entities.FriendItem
import com.kyotob.client.repositories.remoteUtil.CommonInterceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class UsersRemoteDataSource {

    val gson = GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()

    private val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(OkHttpClient.Builder().addInterceptor(CommonInterceptor()).build())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    private val api = retrofit.create(UserApi::class.java)

    fun updateUser(name: String, accessToken: String, newName: String): Call<Unit> {
        return api.putName(name, accessToken, hashMapOf("new_screen_name" to newName))
    }

    fun register(name: String, screenName: String, password: String) = api.postUser(createRegisterRequest(name,screenName,password))

    fun createRegisterRequest(name: String, screenName: String, password: String): HashMap<String,String> {
        val hashMap: HashMap<String,String> = HashMap()
        hashMap.put("name", name)
        hashMap.put("screen_name", screenName)
        hashMap.put("password", password)
        return hashMap
    }

    fun createLoginRequest(name: String, password: String): HashMap<String,String> {
        val hashMap: HashMap<String,String> = HashMap()
        hashMap.put("name", name)
        hashMap.put("password", password)
        return hashMap
    }

    fun login(name:String, password: String) = api.loginUser(createLoginRequest(name,password))

    fun getFriendList(name: String, token: String) = api.getFriendList(name, token)

    fun postGroupRoom(token: String, roomName: String, memberList: List<String>): Call<Unit> {
        val postGroupRoomRequest = PostGroupRoomRequest(roomName, memberList.map { hashMapOf("user_name" to it)})
        return api.postGroupRoom(token, postGroupRoomRequest)
    }
}
