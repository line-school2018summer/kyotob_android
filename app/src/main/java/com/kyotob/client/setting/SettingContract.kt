package com.kyotob.client.setting

import com.kyotob.client.BasePresenter
import com.kyotob.client.BaseView

interface SettingContract {

    interface View: BaseView<Presenter> {

        fun showFieldTitle()

        fun showFieldContent(id: String, name: String)

        fun showIcon(imageUrl: String)

        fun showUpdateName()
    }

    interface  Presenter: BasePresenter {

        fun updateIcon()

        fun updateName()
    }
}