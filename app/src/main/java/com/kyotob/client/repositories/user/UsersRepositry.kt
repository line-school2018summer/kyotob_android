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
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.async
import kotlin.coroutines.experimental.coroutineContext

class UsersRepositry(
        val usersRemoteDataSource: UsersRemoteDataSource = UsersRemoteDataSource()
) {


    fun updateUserName(name: String, accessToken: String, newName: String) = usersRemoteDataSource.updateUser(name, accessToken, newName)
}