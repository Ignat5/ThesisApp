package com.mycompany.thesis1.mvvm.requests.resolved

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.mycompany.thesis1.R
import com.mycompany.thesis1.mvvm.model.CommonResource
import kotlinx.android.synthetic.main.fragment_outcoming_requests.*
import kotlinx.android.synthetic.main.fragment_outcoming_requests.progressBar
import kotlinx.coroutines.flow.collect

class OutcomingRequestsFragment: Fragment(R.layout.fragment_outcoming_requests) {

    private val viewModel by viewModels<OutcomingRequestsViewModel>()
    private var adapter: OutcomingListAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initDataListeners()
    }

    private fun initDataListeners() {
        viewModel.outcomingRequests.observe(viewLifecycleOwner) {
            progressBar.visibility = View.GONE
            if(it.isEmpty()) tvMessage.visibility = View.VISIBLE
            else tvMessage.visibility = View.GONE
            adapter?.submitList(it)
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.eventsFlow.collect { resource ->
                when (resource) {
                    is CommonResource.ShowDownload -> progressBar.visibility = View.VISIBLE
                    is CommonResource.Error -> {
                        Log.d("myTag", "initDataListeners: error: ${resource.message}")
                        progressBar.visibility = View.GONE
                        resource.message?.let {
                            Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
                        }
                    }
                    is CommonResource.ShowMessage -> {
                        progressBar.visibility = View.GONE
                        resource.message?.let {
                            Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
                        }
                    }
                    is CommonResource.Success -> {
                        progressBar.visibility = View.GONE
                        resource.message?.let {
                            Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun initRecyclerView() {
        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        rvOutcomingRequests.layoutManager = linearLayoutManager
        adapter = OutcomingListAdapter()
        rvOutcomingRequests.adapter = adapter
    }

}