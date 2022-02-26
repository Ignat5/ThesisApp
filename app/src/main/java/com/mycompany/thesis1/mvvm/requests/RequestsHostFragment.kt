package com.mycompany.thesis1.mvvm.requests

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.mycompany.thesis1.R
import com.mycompany.thesis1.mvvm.requests.active.IncomingRequestsFragment
import com.mycompany.thesis1.mvvm.requests.resolved.OutcomingRequestsFragment
import com.mycompany.thesis1.mvvm.users.add_user.HostViewPagerAdapter
import kotlinx.android.synthetic.main.fragment_users_host.*
import kotlinx.android.synthetic.main.layout_toolbar.*

class RequestsHostFragment: Fragment(R.layout.fragment_requests_host) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initTabs()
        initDataListeners()
    }

    private fun initViews() {
        tvScreenTitle.text = "Запросы"
        ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun initDataListeners() {

    }

    private fun initTabs() {
        val adapter = HostViewPagerAdapter(childFragmentManager, lifecycle)
        adapter.addFragment(IncomingRequestsFragment(), "Входящие запросы")
        adapter.addFragment(OutcomingRequestsFragment(), "Исходящие запросы")
        vpScreen.adapter = adapter
        TabLayoutMediator(tlOptions, vpScreen) { tab, position ->
            tab.text = adapter.fragmentTitleList[position]
        }.attach()
    }

}