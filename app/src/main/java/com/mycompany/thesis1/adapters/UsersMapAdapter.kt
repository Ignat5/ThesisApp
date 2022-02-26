package com.mycompany.thesis1.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mycompany.thesis1.R
import com.mycompany.thesis1.mvvm.model.User
import kotlinx.android.synthetic.main.item_user_map.view.*
import java.text.SimpleDateFormat
import java.util.*

class UsersMapAdapter(val listener: Listener): RecyclerView.Adapter<UsersMapAdapter.UsersViewHolder>() {

    interface Listener {
        fun onClicked(userId: String)
    }

    private val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm")
    private val listOfUsers = mutableListOf<User>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        val item = LayoutInflater.from(parent.context).inflate(R.layout.item_user_map, parent, false)
        return UsersViewHolder(item)
    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        holder.bind(listOfUsers[position])
    }

    fun setUsers(users: List<User>) {
        listOfUsers.clear()
        listOfUsers.addAll(users)
        this.notifyDataSetChanged()
    }

    fun updateUsers(users: List<User>) {
        if(users.size == 1) {
            val position = listOfUsers.indexOfFirst { it.userId == users[0].userId }
            listOfUsers[position].updatedAt = users[0].updatedAt
            this.notifyItemChanged(position)
        } else {
            for(user in users) {
                val position = listOfUsers.indexOfFirst { it.userId == user.userId }
                listOfUsers[position].updatedAt = user.updatedAt
            }
        }
    }

    override fun getItemCount(): Int = listOfUsers.size

    inner class UsersViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bind(user: User) {
            val time = user.updatedAt?.seconds?.times(1000) ?: 0
            Log.d("myTag", "bind: user: ${user.userName}, time: $time")
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

}