package com.mycompany.thesis1.mvvm.users

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mycompany.thesis1.R
import com.mycompany.thesis1.mvvm.model.User
import kotlinx.android.synthetic.main.item_group_user.view.*

class GroupUsersListAdapter(val listener: Listener): ListAdapter<User, GroupUsersListAdapter.GroupsViewHolder>(DiffCallback()) {

    interface Listener {
        fun onDeleteUserFromGroup(user: User)
    }

    inner class GroupsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bind(user: User) {
            itemView.apply {
                tvUserName.text = user.userName
                tvUserEmail.text = user.userId
            }
            itemView.clRoot.setOnClickListener {
                itemView.ivOptions.rotation = 180F
                val popup = PopupMenu(itemView.context, itemView.clRoot, Gravity.END)
                popup.menuInflater.inflate(R.menu.group_users_menu, popup.menu)
                popup.setOnMenuItemClickListener { menuItem ->
                    when(menuItem?.itemId) {
                        R.id.miDeleteFromGroup -> listener.onDeleteUserFromGroup(user)
                    }
                    false
                }
                popup.setOnDismissListener {
                    itemView.ivOptions.rotation = 0F
                }
                popup.show()
            }
        }
    }

    override fun submitList(list: List<User>?) {
        super.submitList(list?.let { ArrayList(it) })
    }

    class DiffCallback: DiffUtil.ItemCallback<User>() {
        override fun areContentsTheSame(oldItem: User, newItem: User) =
            oldItem == newItem

        override fun areItemsTheSame(oldItem: User, newItem: User) =
            oldItem.userId == newItem.userId
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupsViewHolder {
        val item = LayoutInflater.from(parent.context).inflate(R.layout.item_group_user, parent, false)
        return GroupsViewHolder(item)
    }

    override fun onBindViewHolder(holder: GroupsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}