package com.kyotob.client.setting

import com.kyotob.client.BasePresenter
import com.kyotob.client.BaseView

interface NameContract {

    interface View: BaseView<Presenter> {

        var isActive: Boolean

        fun showName(name: String)

        fun showToast(message: String)

        fun hideKeyBoard()

        fun finish()

    }

    interface  Presenter: BasePresenter {

        suspend fun updateName(newName: String)

        fun clearText()
    }
}