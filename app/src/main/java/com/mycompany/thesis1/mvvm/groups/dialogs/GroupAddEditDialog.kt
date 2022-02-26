package com.mycompany.thesis1.mvvm.groups.dialogs

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import com.mycompany.thesis1.R
import kotlinx.android.synthetic.main.dialog_group_add_edit.view.*


class GroupAddEditDialog(
    val groupName: String = "",
    val action: GroupAction,
    val listener: Listener
): DialogFragment() {

    interface Listener {
        fun onChangeGroupName(updatedCurrentName: String)
        fun onCreateGroup(groupName: String)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view =
            LayoutInflater.from(activity)
                .inflate(R.layout.dialog_group_add_edit, null, false)
        val builder = AlertDialog.Builder(requireContext(),R.style.MyRoundedCornersDialog)
        builder.setView(view)
        initUI(view)
        return builder.create()
    }
    private fun initUI(view:View) {
        view.btnCancel.setOnClickListener {
            dismiss()
        }
        when(action) {
            is GroupAction.ADD_GROUP -> {
                view.tvTitle.text = "Введите название новой группы"
                view.btnChoose.text = "Создать"
                view.btnChoose.setOnClickListener {
                    val newGroupName = view.etGroupName.text.toString()
                    listener.onCreateGroup(newGroupName)
                    dismiss()
                }
                view.etGroupName.addTextChangedListener {
                    it?.toString()?.let { text ->
                        Log.d("myTag1", "textChangedListener: $it")
                        if(text.isBlank()) {
                            view.btnChoose.apply {
                                isEnabled = false
                                setTextColor(ContextCompat.getColor(requireContext(), R.color.black_33))
                            }
                        } else {
                            view.btnChoose.apply {
                                isEnabled = true
                                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                            }
                        }
                    }
                }

            }
            is GroupAction.EDIT_GROUP -> {
                view.tvTitle.text = "Введите новое название группы"
                view.btnChoose.text = "Изменить"
                view.etGroupName.setText(groupName)
                view.btnChoose.setOnClickListener {
                    val updatedGroupName = view.etGroupName.text.toString()
                    listener.onChangeGroupName(updatedGroupName)
                    dismiss()
                }
                view.etGroupName.addTextChangedListener {
                    it?.toString()?.let { text ->
                        Log.d("myTag1", "textChangedListener: $it")
                        if(text.isBlank() or text.equals(groupName,false)) {
                            view.btnChoose.apply {
                                isEnabled = false
                                setTextColor(ContextCompat.getColor(requireContext(), R.color.black_33))
                            }
                        } else {
                            view.btnChoose.apply {
                                isEnabled = true
                                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                            }
                        }
                    }
                }
            }
        }
    }
    sealed class GroupAction() {
        object ADD_GROUP: GroupAction()
        object EDIT_GROUP: GroupAction()
    }
}