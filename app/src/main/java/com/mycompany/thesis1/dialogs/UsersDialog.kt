package com.mycompany.thesis1.dialogs

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mycompany.thesis1.R
import com.mycompany.thesis1.adapters.UsersMapAdapter
import com.mycompany.thesis1.mvvm.model.User
import kotlinx.android.synthetic.main.dialog_users.*

class UsersDialog(
    private val listOfUsers: List<User>,
    private val listener: Listener
): BottomSheetDialogFragment() {

    interface Listener {
        fun onClicked(userId: String)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext())
        //dialog.behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
    }

    private fun initRecyclerView() {
        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        rvUsers.layoutManager = linearLayoutManager
        rvUsers.setHasFixedSize(true)
        val adapter = UsersMapAdapter(object: UsersMapAdapter.Listener {
            override fun onClicked(userId: String) {
                Log.d("myTag", "onClicked: userId: $userId")
                listener.onClicked(userId)
            }
        })
        rvUsers.adapter = adapter
        adapter.setUsers(listOfUsers)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return inflater.inflate(R.layout.dialog_users, container, false)
    }

}