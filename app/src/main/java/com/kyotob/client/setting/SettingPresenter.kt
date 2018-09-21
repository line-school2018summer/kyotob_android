package com.kyotob.client.setting

import android.content.SharedPreferences
import com.kyotob.client.USER_IMAGE_URL_KEY
import com.kyotob.client.USER_NAME_KEY
import com.kyotob.client.USER_SCREEN_NAME_KEY

class SettingPresenter(
        private val settingView: SettingContract.View,
        private val sharedPreferences: SharedPreferences
) : SettingContract.Presenter{

    init {
        settingView.presenter = this
    }

    override fun start() {
        openSetting()
    }

    private fun openSetting() {
        val name = sharedPreferences.getString(USER_NAME_KEY, "default")
        val screenName = sharedPreferences.getString(USER_SCREEN_NAME_KEY, "default")
        val imageUrl = sharedPreferences.getString(USER_IMAGE_URL_KEY, "abc.png")
        settingView.showFieldTitle()
        settingView.showFieldContent(name!!, screenName!!)
        settingView.showIcon(imageUrl)
    }

    override fun updateIcon() {
        //todo
    }

    override fun updateName() {
        settingView.showUpdateName()
    }
}
