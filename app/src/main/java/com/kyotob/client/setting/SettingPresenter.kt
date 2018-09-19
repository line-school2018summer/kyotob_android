package com.kyotob.client.setting

import android.content.SharedPreferences
import com.kyotob.client.USERNAMEKEY
import com.kyotob.client.USERSCREENNAMEKEY

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
        val name = sharedPreferences.getString(USERNAMEKEY, "default")
        val screenName = sharedPreferences.getString(USERSCREENNAMEKEY, "default")
        settingView.showFieldTitle()
        settingView.showFieldContent(name!!, screenName!!)
    }

    override fun updateIcon() {
        //todo
    }

    override fun updateName() {
        settingView.showUpdateName()
    }
}