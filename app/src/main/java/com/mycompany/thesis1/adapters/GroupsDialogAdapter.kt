package com.mycompany.thesis1.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mycompany.thesis1.R
import com.mycompany.thesis1.mvvm.model.Group
import kotlinx.android.synthetic.main.item_set_group.view.*

class GroupsDialogAdapter(val listener: Listener): RecyclerView.Adapter<GroupsDialogAdapter.GroupDialogViewHolder>() {

    interface Listener {
        fun onChange(chosenGroupId: String)
    }

    private val listOfGroup = mutableListOf<Group>()
    private var currentGroupId = ""
    private val groupMap = mutableMapOf<String, View>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupDialogViewHolder {
        val item = LayoutInflater.from(parent.context).inflate(R.layout.item_set_group, parent, false)
        return GroupDialogViewHolder(item)
    }

    override fun onBindViewHolder(holder: GroupDialogViewHolder, position: Int) {
        groupMap[listOfGroup[position].groupId] = holder.itemView
        holder.bind(listOfGroup[position])
    }

    fun setGroups(groups: List<Group>) {
        listOfGroup.clear()
        listOfGroup.addAll(groups)
        this.notifyDataSetChanged()
    }

    override fun getItemCount(): Int = listOfGroup.size

    inner class GroupDialogViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bind(group: Group) {
            itemView.tvGroupName.text = group.groupName
            if(group.isCurrent) {
                currentGroupId = group.groupId
                itemView.ivCurrent.visibility = View.VISIBLE
                itemView.tvGroupName.setTextColor(ContextCompat.getColor(itemView.context,R.color.dark_green))
            } else {
                itemView.ivCurrent.visibility = View.INVISIBLE
                itemView.tvGroupName.setTextColor(ContextCompat.getColor(itemView.context,R.color.black_40))
            }
            itemView.clRoot.setOnClickListener {
                if(group.groupId != currentGroupId) {
                    itemView.ivCurrent.visibility = View.VISIBLE
                    itemView.tvGroupName.setTextColor(ContextCompat.getColor(itemView.context,R.color.dark_green))
                    groupMap[currentGroupId]?.apply {
                        ivCurrent.visibility = View.INVISIBLE
                        tvGroupName.setTextColor(ContextCompat.getColor(itemView.context,R.color.black_40))
                    }
                    currentGroupId = group.groupId
                    listener.onChange(group.groupId)
                }
            }
//            itemView.clRoot.setOnClickListener {
//                if(group.isCurrent.not()) {
//                    listOfGroup.find { group -> group.isCurrent }?.apply{
//                        isCurrent = false
//                    }
//                    group.isCurrent = true
//                    notifyChange()
//                    listener.onChange(group.groupId)
//                }
//            }
        }
    }

}