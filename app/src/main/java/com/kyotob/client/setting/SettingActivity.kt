package com.kyotob.client.setting

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.kyotob.client.R
import com.kyotob.client.util.replaceFragmentInActivity

class SettingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        // Fragment設定
        val settingFragment = supportFragmentManager
                .findFragmentById(R.id.contentFrame) as SettingFragment? ?:
                SettingFragment().also {
                    replaceFragmentInActivity(it, R.id.contentFrame)
                }

        val settingPresenter = SettingPresenter(settingFragment, getSharedPreferences("userData", Context.MODE_PRIVATE))
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    companion object {
        const val EXTRA_USER = "USER"
    }
}
