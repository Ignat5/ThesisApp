package com.mycompany.thesis1.mvvm.users

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.mycompany.thesis1.R
import com.mycompany.thesis1.mvvm.model.CommonResource
import com.mycompany.thesis1.mvvm.model.User
import kotlinx.android.synthetic.main.fragment_group_users.*
import kotlinx.android.synthetic.main.layout_toolbar.*
import kotlinx.coroutines.flow.collect

class GroupUsersFragment: Fragment(R.layout.fragment_group_users) {

    private val viewModel by viewModels<GroupUsersViewModel>()
    private var adapter: GroupUsersListAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initDataListeners()
        initRecyclerView()
        initViews()
    }

    private fun initDataListeners() {
        viewModel.users.observe(viewLifecycleOwner) { users ->
            progressBar.visibility = View.GONE
            adapter?.submitList(users)
            tvNoUsersMessage.visibility =
            if (users.isEmpty()) View.VISIBLE
            else View.GONE
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.eventsFlow.collect { resource ->
                when (resource) {
                    is CommonResource.ShowDownload -> { progressBar.visibility = View.VISIBLE }
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
                }
            }
        }
    }

    private fun initRecyclerView() {
        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        rvGroupUsers.layoutManager = linearLayoutManager
        adapter = GroupUsersListAdapter(object: GroupUsersListAdapter.Listener {
            override fun onDeleteUserFromGroup(user: User) {
                MaterialAlertDialogBuilder(requireContext(), R.style.MyThemeOverlay_MaterialComponents_MaterialAlertDialog)
                    .setTitle("Подтверждение")
                    .setMessage("Вы действительно хотите удалить ${user.userName} из группы?")
                    .setPositiveButton("Да") { _, _ ->
                        viewModel.deleteUserFromGroup(user.userId)
                    }
                    .setNegativeButton("Нет") { _, _ -> }
                    .show()
            }
        })
        rvGroupUsers.adapter = adapter
    }

    private fun initViews() {
        tvScreenTitle.text = "Участники"
        ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
        fabAddUser.setOnClickListener {
            viewModel.getCurrentGroupId()?.let {
                val action = GroupUsersFragmentDirections.actionGroupUsersFragmentToAddUserHostFragment(it)
                findNavController().navigate(action)
            }
        }
    }
}