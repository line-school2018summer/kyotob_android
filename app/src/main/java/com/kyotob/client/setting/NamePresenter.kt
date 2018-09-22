package com.kyotob.client.setting

import android.content.SharedPreferences
import com.kyotob.client.*
import com.kyotob.client.repositories.user.UsersRepository
import kotlinx.coroutines.experimental.*
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
        view.showName(sharedPreferences.getString(USER_SCREEN_NAME_KEY,"default")!!)
    }

    override suspend fun updateName(newName: String) {
        val token = sharedPreferences.getString(TOKEN_KEY,"default")
        val name = sharedPreferences.getString(USER_NAME_KEY, "default")
        val iconPath = sharedPreferences.getString(USER_IMAGE_URL_KEY, "abc.png")
        try {
            withContext(CommonPool) {
                usersRepository.updateUserName(name!!, token!!, newName, iconPath!!).awaitResponse()
            }
            val editor = sharedPreferences.edit()
            editor.putString(USER_SCREEN_NAME_KEY, newName)
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