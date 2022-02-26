package com.mycompany.thesis1.mvvm.groups

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
import com.mycompany.thesis1.mvvm.groups.dialogs.GroupAddEditDialog
import com.mycompany.thesis1.mvvm.model.CommonResource
import com.mycompany.thesis1.mvvm.model.Group
import kotlinx.android.synthetic.main.fragment_groups.*
import kotlinx.android.synthetic.main.layout_toolbar.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class GroupsFragment: Fragment(R.layout.fragment_groups) {

    private val viewModel by viewModels<GroupsViewModel>()
    private var adapter: GroupsListAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initDataListeners()
        initRecyclerView()
        initViews()
        Log.d("myTag", "GroupsFragment: onViewCreated...")
    }

    private fun initDataListeners() {
        viewModel.groups.observe(viewLifecycleOwner) { groups ->
            progressBar.visibility = View.GONE
            adapter?.submitList(groups)
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.eventFlow.collect { resource ->
                when(resource) {
                    is CommonResource.ShowDownload -> {
                        progressBar.visibility = View.VISIBLE
                    }
                    is CommonResource.Error -> {
                        resource.message?.let {
                            Snackbar.make(requireView(),resource.message, Snackbar.LENGTH_SHORT).show()
                        }
                        progressBar.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun initViews() {
        progressBar.visibility = View.VISIBLE
        tvScreenTitle.text = "Группы"
        ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
        fabAddGroup.setOnClickListener {
            val dialog = GroupAddEditDialog(
                action = GroupAddEditDialog.GroupAction.ADD_GROUP,
                listener = object : GroupAddEditDialog.Listener {
                    override fun onChangeGroupName(updatedCurrentName: String) {}
                    override fun onCreateGroup(groupName: String) {
                        viewModel.createGroup(groupName)
                    }
                }
            )
            dialog.isCancelable = true
            dialog.show(requireActivity().supportFragmentManager, "TAG_CREATE_GROUP_DIALOG")
        }
    }

    private fun initRecyclerView() {
        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        rvGroups.layoutManager = linearLayoutManager
        adapter = GroupsListAdapter(object: GroupsListAdapter.Listener {
            override fun onUsersClicked(groupId: String) {
                Log.d("myTag", "adapter_listener: miUsers for $groupId")
                val action = GroupsFragmentDirections.actionGroupsFragmentToGroupUsersFragment(groupId)
                findNavController().navigate(action)
            }

            override fun onChangeGroupNameClicked(groupId: String) {
                Log.d("myTag", "adapter_listener: miChangeName for $groupId")
                viewModel.getGroupById(groupId)?.let {
                    val dialog = GroupAddEditDialog(
                        groupName = it.groupName,
                        action = GroupAddEditDialog.GroupAction.EDIT_GROUP,
                        listener = object: GroupAddEditDialog.Listener {
                            override fun onChangeGroupName(updatedCurrentName: String) {
                                Log.d("myTag", "onChangeGroupName: $updatedCurrentName")
                                viewModel.updateGroupName(groupId, updatedCurrentName)
                            }
                            override fun onCreateGroup(groupName: String) {}
                        }
                    )
                    dialog.isCancelable = true
                    dialog.show(requireActivity().supportFragmentManager, "TAG_CHANGE_GROUP_NAME_DIALOG")
                }
            }

            override fun onDeleteGroupClicked(group: Group) {
                MaterialAlertDialogBuilder(requireContext(), R.style.MyThemeOverlay_MaterialComponents_MaterialAlertDialog)
                    .setTitle("Подтверждение")
                    .setMessage("Вы действительно хотите удалить группу ${group.groupName} ?")
                    .setPositiveButton("Да") { _, _ ->
                        Log.d("myTag", "Delete group with id: ${group.groupId}")
                        if(!group.isCurrent) viewModel.deleteGroupById(group.groupId)
                    }
                    .setNegativeButton("Нет") { _, _ -> }
                    .show()
            }
        })
        rvGroups.adapter = adapter
    }

}