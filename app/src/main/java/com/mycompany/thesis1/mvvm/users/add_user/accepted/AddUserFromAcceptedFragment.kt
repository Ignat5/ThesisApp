package com.mycompany.thesis1.mvvm.users.add_user.accepted

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
import com.mycompany.thesis1.mvvm.model.User
import kotlinx.android.synthetic.main.fragment_add_user_from_accepted.*
import kotlinx.coroutines.flow.collect

class AddUserFromAcceptedFragment(val groupId: String): Fragment(R.layout.fragment_add_user_from_accepted) {

    private val viewModel by viewModels<AddUserFromAcceptedViewModel>()
    private var adapter: AcceptedUsersAdapter? = null
    private val addedUsersId = mutableListOf<String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.start(groupId)
        initRecyclerView()
        initViews()
        initDataListeners()
    }

    private fun initDataListeners() {
        viewModel.users.observe(viewLifecycleOwner) {
            progressBar.visibility = View.GONE
            if (it.isEmpty()) tvNoUsersMessage.visibility = View.VISIBLE
            adapter?.setRequests(it)
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.eventsFlow.collect { resource ->
                when (resource) {
                    is CommonResource.ShowDownload -> progressBar.visibility = View.VISIBLE
                    is CommonResource.Error ->
                        resource.message?.let {
                            progressBar.visibility = View.GONE
                            Snackbar.make(requireView(), resource.message, Snackbar.LENGTH_LONG).show()
                        }
                    is CommonResource.Success ->
                        resource.message?.let {
                            progressBar.visibility = View.GONE
                            Snackbar.make(requireView(), resource.message, Snackbar.LENGTH_LONG).show()
                        }
                }
            }
        }
    }

    private fun initViews() {
        fabAdd.setOnClickListener {
            Log.d("myTag", "fabAdd users: $addedUsersId")
            viewModel.addToGroup(groupId, addedUsersId)
        }
    }

    private fun initRecyclerView() {
        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        rvUsers.layoutManager = linearLayoutManager
        adapter = AcceptedUsersAdapter(groupId, object: AcceptedUsersAdapter.Listener {
            override fun onClick(chosenUsers: List<String>) {
                addedUsersId.clear()
                addedUsersId.addAll(chosenUsers)
                fabAdd.visibility =
                if (addedUsersId.isEmpty()) View.GONE
                else View.VISIBLE
            }
        })
        rvUsers.adapter = adapter
    }

}