package com.kyotob.client.setting

import android.content.Intent
import android.net.Uri
import com.kyotob.client.BasePresenter
import com.kyotob.client.BaseView

interface SettingContract {

    interface View: BaseView<Presenter> {

        fun showFieldTitle()

        fun showFieldContent(id: String, name: String)

        fun showIcon(imagePath: String)

        fun showUpdateName()

        fun showUpdateIcon()

        fun showToast(message: String)
    }

    interface  Presenter: BasePresenter {

        fun updateIcon(uri: Uri)

        fun updateName()

        fun onIconClick()

        fun setDefaultIcon()
    }
}