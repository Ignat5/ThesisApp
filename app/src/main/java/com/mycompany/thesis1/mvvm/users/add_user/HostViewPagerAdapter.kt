package com.mycompany.thesis1.mvvm.users.add_user

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class HostViewPagerAdapter(supportFragmentManager: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(supportFragmentManager, lifecycle) {

    private val fragmentList = arrayListOf<Fragment>()
    val fragmentTitleList = arrayListOf<String>()

    fun addFragment(fragment: Fragment, title: String) {
        fragmentList.add(fragment)
        fragmentTitleList.add(title)
    }

    override fun createFragment(position: Int): Fragment = fragmentList[position]

    override fun getItemCount(): Int = fragmentList.size
}