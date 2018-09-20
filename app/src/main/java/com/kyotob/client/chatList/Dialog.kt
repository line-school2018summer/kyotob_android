package com.kyotob.client.chatList

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.app.AlertDialog
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import com.kyotob.client.R

// Dialogの諸々の設定ををするクラス
class Dialog : DialogFragment() {

    lateinit var mpager: ViewPager
    lateinit var tabLayout: TabLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // AlertDialogをつくる
        //val builder = AlertDialog.Builder(activity)
        // dialog.xmlをつかって、ダイアログをデザインする
        //val inflater = activity!!.layoutInflater.inflate(R.layout.dialog, null)
        val view = inflater.inflate(R.layout.dialog, container, false)


        //tabの設定
        mpager = view.findViewById(R.id.viewpager)
        mpager.adapter = TabAdapter(childFragmentManager)
        tabLayout = view.findViewById(R.id.tabs)
        tabLayout.setupWithViewPager(mpager)

        // builderにビューをセットする
        //builder.setView(inflater)
        // builderを返す
        //return builder.create()

        dialog.window.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(true)

        return view
    }
}
