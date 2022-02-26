package com.mycompany.thesis1.mvvm.users.add_user

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.mycompany.thesis1.R
import com.mycompany.thesis1.mvvm.users.add_user.accepted.AddUserFromAcceptedFragment
import com.mycompany.thesis1.mvvm.users.add_user.search.AddUserSearchFragment
import kotlinx.android.synthetic.main.fragment_users_host.*
import kotlinx.android.synthetic.main.layout_toolbar.*

class AddUserHostFragment: Fragment(R.layout.fragment_users_host) {

    private lateinit var groupId: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initTabs()
    }

    private fun initViews() {
        tvScreenTitle.text = "Добавить участника"
        ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
        arguments?.let {
            AddUserHostFragmentArgs.fromBundle(it).let { args ->
                groupId = args.groupId
            }
        }
    }

    private fun initTabs() {
        val adapter = HostViewPagerAdapter(childFragmentManager, lifecycle)
        adapter.addFragment(AddUserFromAcceptedFragment(groupId), "Принявшие запрос")
        adapter.addFragment(AddUserSearchFragment(groupId), "Отправить запрос")
        vpScreen.adapter = adapter
        TabLayoutMediator(tlOptions, vpScreen) { tab, position ->
            tab.text = adapter.fragmentTitleList[position]
        }.attach()
    }

}