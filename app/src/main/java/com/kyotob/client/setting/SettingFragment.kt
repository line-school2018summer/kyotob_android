package com.kyotob.client.setting

import android.app.Activity
import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.ImageButton
import android.support.constraint.ConstraintLayout
import android.widget.Button
import com.bumptech.glide.Glide
import com.kyotob.client.R
import com.kyotob.client.baseUrl


class SettingFragment: Fragment(), SettingContract.View {

    private lateinit var idLayout: ConstraintLayout
    private lateinit var nameLayout: ConstraintLayout
    private lateinit var idTitle: TextView
    private lateinit var idContent: TextView
    private lateinit var nameTitle: TextView
    private lateinit var nameContent: TextView
    private lateinit var icon: ImageButton
    private lateinit var nameUpdateButton: Button

    override lateinit var presenter: SettingContract.Presenter

    override fun onResume() {
        super.onResume()
        presenter.start()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_setting, container, false)
        setHasOptionsMenu(true)
        with(root) {
            idLayout = findViewById(R.id.id_view)
            idTitle = idLayout.findViewById(R.id.title)
            idContent = idLayout.findViewById(R.id.content)
            nameLayout = findViewById(R.id.name_view)
            nameTitle = nameLayout.findViewById(R.id.title)
            nameContent = nameLayout.findViewById(R.id.content)
            icon = findViewById(R.id.icon)
            nameUpdateButton = findViewById(R.id.name_button)
        }

        icon.setOnClickListener {
            presenter.updateIcon()
        }

        nameLayout.setOnClickListener {
            presenter.updateName()
        }

        nameUpdateButton.setOnClickListener {
            presenter.updateName()
        }

        return root
    }

    override fun showIcon(imageUrl: String) {
        Glide.with(this).load(baseUrl+"image/download/" + imageUrl).into(icon)
    }

    override fun showFieldTitle() {
        idTitle.text = "ID"
        nameTitle.text = "Name"
    }

    override fun showFieldContent(id: String, name: String) {
        idContent.text = id
        nameContent.text = name
    }

    override fun showUpdateName() {
        startActivity(Intent(context, NameActivity::class.java))
    }

}