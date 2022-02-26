package com.mycompany.thesis1.adapters

import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.mycompany.thesis1.R
import com.mycompany.thesis1.mvvm.model.Group
import kotlinx.android.synthetic.main.item_group.view.*

class GroupsAdapter(val listener: Listener): RecyclerView.Adapter<GroupsAdapter.GroupViewHolder>() {

    interface Listener {
        fun onGroupClicked(groupId: String)
    }

    private val listOfGroup = mutableListOf<Group>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val item = LayoutInflater.from(parent.context).inflate(R.layout.item_group, parent, false)
        return GroupViewHolder(item)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        holder.bind(listOfGroup[position])
    }

    override fun getItemCount() = listOfGroup.size

    fun setGroups(groups: List<Group>) {
        listOfGroup.clear()
        listOfGroup.addAll(groups)
        this.notifyDataSetChanged()
    }

    inner class GroupViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bind(group: Group) {
            itemView.tvGroupName.text = group.groupName
            itemView.clRoot.setOnClickListener {
                //listener.onGroupClicked(group.groupId)
                itemView.ivOptions.rotation = 180F
                val popup = PopupMenu(itemView.context, itemView.clRoot, Gravity.END)
                popup.menuInflater.inflate(R.menu.group_options, popup.menu)
                popup.setOnMenuItemClickListener { menuItem ->
                    when(menuItem?.itemId) {
                        R.id.miUsers -> {
                            Log.d("myTag", "bind: miUsers")
                        }
                        R.id.miChangeName -> {
                            Log.d("myTag", "bind: miChangeName")
                        }
                        R.id.miDelete -> {
                            Log.d("myTag", "bind: miDelete")
                        }
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

}