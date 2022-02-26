package com.mycompany.thesis1.mvvm.users.add_user.accepted

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mycompany.thesis1.R
import com.mycompany.thesis1.mvvm.model.Request
import kotlinx.android.synthetic.main.item_accepted_user.view.*

class AcceptedUsersAdapter(val currentGroupId: String, val listener: Listener): RecyclerView.Adapter<AcceptedUsersAdapter.AcceptedUsersViewHolder>() {

    interface Listener {
        fun onClick(chosenUsers: List<String>)
    }

    private val requests = mutableListOf<Pair<Request, Boolean>>()
    private val chosenUsersId = mutableListOf<String>()

    override fun onBindViewHolder(holder: AcceptedUsersViewHolder, position: Int) {
        holder.bind(requests[position])
    }

    inner class AcceptedUsersViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bind(request: Pair<Request, Boolean>) {
            itemView.apply {
                request.first.info.to.let {
                    tvUserName.text = it.name
                    tvUserEmail.text = it.email
                }
                if (request.second) {
                    tvMessage.visibility = View.VISIBLE
                    checkBox.visibility = View.GONE
                    tvUserName.setTextColor(ContextCompat.getColor(itemView.context, R.color.black_60))
                    itemView.clRoot.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.white))
                }
            }
            request.first.info.to.email.let { toId ->
                itemView.clRoot.setOnClickListener {
                    if (chosenUsersId.contains(toId)) {
                        chosenUsersId.remove(toId)
                        itemView.checkBox.isChecked = false
                        itemView.clRoot.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.white))
                        listener.onClick(chosenUsersId)
                    } else if(request.second.not()){
                        chosenUsersId.add(toId)
                        itemView.checkBox.isChecked = true
                        itemView.clRoot.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.purple_200))
                        listener.onClick(chosenUsersId)
                    }
                }
            }
        }
    }

    fun setRequests(newUsers: List<Pair<Request, Boolean>>) {
        requests.clear()
        requests.addAll(newUsers)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AcceptedUsersViewHolder {
        val item = LayoutInflater.from(parent.context).inflate(R.layout.item_accepted_user, parent, false)
        return AcceptedUsersViewHolder(item)
    }

    override fun getItemCount(): Int = requests.size

}