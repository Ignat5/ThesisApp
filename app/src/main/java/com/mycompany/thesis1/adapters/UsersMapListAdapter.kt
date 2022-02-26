package com.mycompany.thesis1.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mycompany.thesis1.R
import com.mycompany.thesis1.mvvm.model.User
import kotlinx.android.synthetic.main.item_user_map.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class UsersMapListAdapter(val listener: Listener): ListAdapter<User, UsersMapListAdapter.UserMapViewHolder>(DiffCallback()) {

    interface Listener {
        fun onClicked(userId: String)
    }

    private val sdf = SimpleDateFormat("dd.MM HH:mm")

    inner class UserMapViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bind(user: User) {
            val time = user.updatedAt?.seconds?.times(1000) ?: 0
            itemView.apply {
                tvUserName.text = user.userName
                tvUserEmail.text = user.userId
                tvLastUpdateTime.text = sdf.format(
                    Date(time)
                )
                clRoot.setOnClickListener {
                    listener.onClicked(user.userId)
                }
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserMapViewHolder {
        val item = LayoutInflater.from(parent.context).inflate(R.layout.item_user_map, parent, false)
        return UserMapViewHolder(item)
    }

    override fun onBindViewHolder(holder: UserMapViewHolder, position: Int) {
        Log.d("myTag", "onBindViewHolder: ${getItem(position)}")
        holder.bind(getItem(position))
    }

}