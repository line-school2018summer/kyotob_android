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
package com.kyotob.client.repositories.user

import com.kyotob.client.repositories.user.remote.UsersRemoteDataSource
import okhttp3.MultipartBody

class UsersRepository(
        private val usersRemoteDataSource: UsersRemoteDataSource = UsersRemoteDataSource()
) {
    fun updateUserName(name: String, accessToken: String, newName: String) = usersRemoteDataSource.updateUser(name, accessToken, newName)

    fun register(name: String, screenName: String, password: String, iconPath: String = "default.jpeg") = usersRemoteDataSource.register(name, screenName, password, iconPath)

    fun login(name: String, password: String) = usersRemoteDataSource.login(name, password)

    fun getFriendList(name: String, token: String) = usersRemoteDataSource.getFriendList(name, token)

    fun postGroupRoomRequest(token: String, roomName: String, memberList: List<String>) = usersRemoteDataSource.postGroupRoom(token, roomName, memberList)

    fun uploadIcon(file: MultipartBody.Part) = usersRemoteDataSource.uploadIcon(file)
}
