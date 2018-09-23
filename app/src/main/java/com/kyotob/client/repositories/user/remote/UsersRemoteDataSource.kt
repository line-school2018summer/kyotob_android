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
import com.kyotob.client.repositories.remoteUtil.CommonInterceptor
import okhttp3.MultipartBody
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

    fun updateUser(name: String, accessToken: String, newName: String, newIconPath: String): Call<Unit> {
        return api.putUserData(name, accessToken, hashMapOf("new_screen_name" to newName, "new_icon_path" to newIconPath))
    }

    fun register(name: String, screenName: String, password: String, iconPath: String) = api.postUser(createRegisterRequest(name,screenName,password, iconPath))

    fun createRegisterRequest(name: String, screenName: String, password: String, iconPath: String): HashMap<String,String> {
        val hashMap: HashMap<String,String> = HashMap()
        hashMap.put("name", name)
        hashMap.put("screen_name", screenName)
        hashMap.put("password", password)
        hashMap.put("image_url", iconPath)
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

    fun postGroupRoom(token: String, roomName: String, memberList: List<String>, iconPath: String): Call<Unit> {
        val postGroupRoomRequest = PostGroupRoomRequest(roomName, memberList.map { hashMapOf("user_name" to it)}, iconPath)
        return api.postGroupRoom(token, postGroupRoomRequest)
    }

    fun uploadIcon(file: MultipartBody.Part) = api.uploadIcon(file)
}
