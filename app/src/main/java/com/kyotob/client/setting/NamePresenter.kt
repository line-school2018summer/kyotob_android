package com.kyotob.client.setting

import android.content.SharedPreferences
import com.kyotob.client.TOKENKEY
import com.kyotob.client.USERNAMEKEY
import com.kyotob.client.USERSCREENNAMEKEY
import com.kyotob.client.repositories.user.UsersRepository
import kotlinx.coroutines.experimental.*
import ru.gildor.coroutines.retrofit.await
import ru.gildor.coroutines.retrofit.awaitResponse

class NamePresenter(
        private val  view: NameContract.View,
        private val usersRepository: UsersRepository,
        private val sharedPreferences: SharedPreferences
): NameContract.Presenter{

    init {
        view.presenter = this
    }

    override fun start() {
        if (!view.isActive) return
        view.showName(sharedPreferences.getString(USERSCREENNAMEKEY,"default")!!)
    }

    override suspend fun updateName(newName: String) {
        val token = sharedPreferences.getString(TOKENKEY,"default")
        val name = sharedPreferences.getString(USERNAMEKEY, "default")
        try {
            withContext(CommonPool) {
                usersRepository.updateUserName(name!!,token!!, newName).awaitResponse()
            }
            val editor = sharedPreferences.edit()
            editor.putString(USERSCREENNAMEKEY, newName)
            editor.apply()

            view.hideKeyBoard()
            view.showToast("変更されました")
            view.finish()
        } catch (t: Throwable) {
            t.message?.let(view::showToast)
        }
    }

    override fun clearText() {
        view.showName("")
    }
}