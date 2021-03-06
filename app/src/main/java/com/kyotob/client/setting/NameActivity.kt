package com.kyotob.client.setting

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.kyotob.client.R
import com.kyotob.client.USER_DATA_KEY
import com.kyotob.client.repositories.user.UsersRepository
import com.kyotob.client.util.replaceFragmentInActivity

class NameActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        // Fragment設定
        val settingFragment = supportFragmentManager
                .findFragmentById(R.id.contentFrame) as NameFragment? ?:
        NameFragment().also {
            replaceFragmentInActivity(it, R.id.contentFrame)
        }

        // Create the presenter
        NamePresenter(settingFragment, UsersRepository(), getSharedPreferences(USER_DATA_KEY, Context.MODE_PRIVATE))

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    companion object {
        const val EXTRA_TOKEN = "TOKEN"
        const val EXTRA_USER_NAME = "USER_NAME"
    }
}
