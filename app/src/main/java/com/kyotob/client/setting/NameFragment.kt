package com.kyotob.client.setting

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.kyotob.client.R
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI

class NameFragment: Fragment(), NameContract.View {

    private lateinit var nameView: TextView
    private lateinit var saveButton: Button
    private lateinit var clearButton: Button

    override lateinit var presenter: NameContract.Presenter

    val job = Job()

    override fun onResume() {
        super.onResume()
        presenter.start()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_name, container, false)
        setHasOptionsMenu(true)
        with(root) {
            nameView = findViewById(R.id.name_view)
            saveButton = findViewById(R.id.save_button)
            clearButton = findViewById(R.id.clear_bottun)
        }

        saveButton.setOnClickListener {
            launch(UI + job) {
                val name = nameView.text.toString()
                presenter.updateName(name)
            }
        }

        clearButton.setOnClickListener {
            presenter.clearText()
        }

        return root
    }

    override var isActive: Boolean = false
        get() = isAdded

    override fun showName(name: String) {
        nameView.text = name
    }

    override fun showToast(message: String) {
        val toast = Toast.makeText(context, message, Toast.LENGTH_LONG)
        toast.show()
    }

    override fun hideKeyBoard() {
        val manager = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        manager.hideSoftInputFromWindow(view!!.windowToken, 0)
    }

    override fun finish() {
        activity!!.finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}