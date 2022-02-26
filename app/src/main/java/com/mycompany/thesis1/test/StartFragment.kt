package com.mycompany.thesis1.test

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.Navigation
import androidx.navigation.Navigator
import androidx.navigation.fragment.findNavController
import com.mycompany.thesis1.R
import kotlinx.android.synthetic.main.fragment_start_test.*

class StartFragment(): Fragment(R.layout.fragment_start_test) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnToAsap.setOnClickListener {
            findNavController().navigate(R.id.action_startFragment_to_newAsapFragment, bundleOf("profileId" to 100100L))
        }
        btnToSearch.setOnClickListener {
            findNavController().navigate(R.id.action_startFragment_to_searchFragment2)
        }
    }

}