package com.mycompany.thesis1.test

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.mycompany.thesis1.R

class NewAsapFragment: Fragment(R.layout.fragment_new_asap_test) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val profileId = arguments?.getLong("profileId")
        Log.d("myTag", "onViewCreated: profileId: $profileId")
    }

}