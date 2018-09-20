package com.kyotob.client.chatList

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter


class TabAdapter(fm: FragmentManager): FragmentPagerAdapter(fm) {

    val PAGE_COUNT = 2
    val tabTitles = arrayOf("友達追加", "グループ作成")

    override fun getCount(): Int {
        return PAGE_COUNT
    }

    override fun getItem(p0: Int): Fragment? {
        when(p0) {
            0 -> return PairFragment()

            else -> return GroupFragment()
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return tabTitles[position]
    }
}