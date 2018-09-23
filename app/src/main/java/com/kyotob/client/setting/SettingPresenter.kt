package com.kyotob.client.setting

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import com.kyotob.client.*
import com.kyotob.client.repositories.user.UsersRepository
import com.kyotob.client.util.createIconUpload
import com.kyotob.client.util.imageActivityResult
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import ru.gildor.coroutines.retrofit.awaitResponse

class SettingPresenter(
        private val settingView: SettingContract.View,
        private val context: Context
) : SettingContract.Presenter{

    lateinit var sharedPreferences: SharedPreferences

    init {
        settingView.presenter = this
    }

    override fun start() {
        sharedPreferences = context.getSharedPreferences(USER_DATA_KEY, Context.MODE_PRIVATE)
        openSetting()
    }

    private fun openSetting() {
        val name = sharedPreferences.getString(USER_NAME_KEY, "default")
        val screenName = sharedPreferences.getString(USER_SCREEN_NAME_KEY, "default")
        val imageUrl = sharedPreferences.getString(USER_IMAGE_URL_KEY, "abc.png")
        settingView.showFieldTitle()
        settingView.showFieldContent(name!!, screenName!!)
        settingView.showIcon(imageUrl!!)
    }

    override fun onIconClick() {
        settingView.showUpdateIcon()
    }

    override fun updateName() {
        settingView.showUpdateName()
    }

    override fun updateIcon(uri: Uri) {
        try {
            launch (UI) {
                val part = createIconUpload(uri, context)
                val response = withContext(CommonPool) { UsersRepository().uploadIcon(part).awaitResponse() }
                if (response.isSuccessful) {
                    val imagePath = response.body()!!.path
                    val name = sharedPreferences.getString(USER_NAME_KEY, "default")!!
                    val token = sharedPreferences.getString(TOKEN_KEY, "default")!!
                    val screenName = sharedPreferences.getString(USER_SCREEN_NAME_KEY, "default")!!
                    withContext(CommonPool) { UsersRepository().updateUserName(name, token, screenName, imagePath).awaitResponse() }
                    val editor = sharedPreferences.edit()
                    editor.putString(USER_IMAGE_URL_KEY, imagePath)
                    editor.apply()
                    settingView.showToast("変更されました")
                    settingView.showIcon(imagePath)
                } else {
                    settingView.showToast(response.code().toString())
                }
            }
        } catch (e: Throwable) {
            settingView.showToast(e.message!!)
        }
    }

    override fun setDefaultIcon() {
        try {
            launch (UI) {
                val imagePath = "abc.png"
                val name = sharedPreferences.getString(USER_NAME_KEY, "default")!!
                val token = sharedPreferences.getString(TOKEN_KEY, "default")!!
                val screenName = sharedPreferences.getString(USER_SCREEN_NAME_KEY, "default")!!
                withContext(CommonPool) { UsersRepository().updateUserName(name, token, screenName, imagePath).awaitResponse() }
                val editor = sharedPreferences.edit()
                editor.putString(USER_IMAGE_URL_KEY, imagePath)
                editor.apply()
                settingView.showToast("変更されました")
                settingView.showIcon(imagePath)
            }
        } catch (e: Throwable) {
            settingView.showToast(e.message!!)
        }
    }
}
