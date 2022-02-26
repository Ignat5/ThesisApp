package com.mycompany.thesis1.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.mycompany.thesis1.R
import com.mycompany.thesis1.adapters.GroupsDialogAdapter
import com.mycompany.thesis1.mvvm.model.Group
import kotlinx.android.synthetic.main.dialog_set_group.view.*

class SetGroupDialog(
    val listOfGroups: List<Group>,
    val currentGroupId: String,
    val listener: Listener
): DialogFragment() {

    private var selectedGroupId = ""

    interface Listener {
        fun onChoose(chosenGroupId: String)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view =
            LayoutInflater.from(activity)
                .inflate(R.layout.dialog_set_group, null, false)
        val builder = AlertDialog.Builder(requireContext(),R.style.MyRoundedCornersDialog)
        builder.setView(view)
        initRecyclerView(view)
        initUI(view)
        return builder.create()
    }
    private fun initRecyclerView(view: View) {
        val recyclerView = view.rvGroups
        val linearLayoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = linearLayoutManager
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.setHasFixedSize(true)
        val adapter = GroupsDialogAdapter(object: GroupsDialogAdapter.Listener {
            override fun onChange(chosenGroupId: String) {
                selectedGroupId = chosenGroupId
                if(selectedGroupId == currentGroupId) {
                    view.btnChoose.isEnabled = false
                    view.btnChoose.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.black_33)
                    )
                } else {
                    view.btnChoose.isEnabled = true
                    view.btnChoose.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.black)
                    )
                }
            }
        })
        recyclerView.adapter = adapter
        adapter.setGroups(listOfGroups)
    }
    private fun initUI(view:View) {
        view.btnChoose.setOnClickListener {
            listener.onChoose(selectedGroupId)
            dismiss()
        }
        view.btnCancel.setOnClickListener { dismiss() }
    }
}