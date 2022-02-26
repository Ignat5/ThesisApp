package com.mycompany.thesis1

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment

class SearchFragment: Fragment(R.layout.fragment_search) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "SearchFragment-onPause: ")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "SearchFragment-onStop: ")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "SearchFragment-onDestroy: ")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "SearchFragment-onResume: ")
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "SearchFragment-onStart: ")
    }

    companion object {
        private const val TAG = "myTag"
    }

}