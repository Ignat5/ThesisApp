package com.mycompany.thesis1.mvvm.groups

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mycompany.thesis1.R
import com.mycompany.thesis1.mvvm.model.Group
import kotlinx.android.synthetic.main.item_group.view.*

class GroupsListAdapter(val listener: Listener): ListAdapter<Group, GroupsListAdapter.GroupsViewHolder>(DiffCallback()) {

    interface Listener {
        fun onUsersClicked(groupId: String)
        fun onChangeGroupNameClicked(groupId: String)
        fun onDeleteGroupClicked(group: Group)
    }

    inner class GroupsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bind(group: Group) {
            itemView.tvGroupName.text = group.groupName
            if(group.isCurrent) itemView.tvGroupName.setTextColor(ContextCompat.getColor(itemView.context, R.color.dark_green))
            itemView.clRoot.setOnClickListener {
                itemView.ivOptions.rotation = 180F
                val popup = PopupMenu(itemView.context, itemView.clRoot, Gravity.END)
                if(group.isCurrent)
                popup.menuInflater.inflate(R.menu.group_options_current, popup.menu)
                else popup.menuInflater.inflate(R.menu.group_options, popup.menu)
                popup.setOnMenuItemClickListener { menuItem ->
                    when(menuItem?.itemId) {
                        R.id.miUsers -> listener.onUsersClicked(group.groupId)
                        R.id.miChangeName -> listener.onChangeGroupNameClicked(group.groupId)
                        R.id.miDelete -> listener.onDeleteGroupClicked(group)
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

    override fun submitList(list: List<Group>?) {
        super.submitList(list?.let { ArrayList(it) })
    }

    class DiffCallback: DiffUtil.ItemCallback<Group>() {
        override fun areContentsTheSame(oldItem: Group, newItem: Group) =
            oldItem == newItem

        override fun areItemsTheSame(oldItem: Group, newItem: Group) =
            oldItem.groupId == newItem.groupId
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupsViewHolder {
        val item = LayoutInflater.from(parent.context).inflate(R.layout.item_group, parent, false)
        return GroupsViewHolder(item)
    }

    override fun onBindViewHolder(holder: GroupsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}