package com.mycompany.thesis1.mvvm.requests.resolved

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mycompany.thesis1.R
import com.mycompany.thesis1.constants.AppConstants.REQUEST_ACCEPTED
import com.mycompany.thesis1.constants.AppConstants.REQUEST_DENIED
import com.mycompany.thesis1.constants.AppConstants.REQUEST_WAITING
import com.mycompany.thesis1.mvvm.model.Request
import kotlinx.android.synthetic.main.item_incoming_request.view.*
import kotlinx.android.synthetic.main.item_outcoming_request.view.*
import kotlinx.android.synthetic.main.item_outcoming_request.view.ivAction
import kotlinx.android.synthetic.main.item_outcoming_request.view.tvUserEmail
import kotlinx.android.synthetic.main.item_outcoming_request.view.tvUserName

class OutcomingListAdapter :
    ListAdapter<Request, OutcomingListAdapter.GroupsViewHolder>(DiffCallback()) {

    inner class GroupsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(request: Request) {
            with(itemView) {
                tvUserName.text = request.info.to.name
                tvUserEmail.text = request.info.to.email
                when (request.status) {
                    REQUEST_ACCEPTED -> {
                        setAction(R.drawable.ic_double_arrow_green)
                        setStatus(R.drawable.ic_check)
                    }
                    REQUEST_WAITING -> {
                        setAction(R.drawable.ic_double_arrow_black)
                        setStatus(R.drawable.ic_wait)
                    }
                    REQUEST_DENIED -> {
                        setAction(R.drawable.ic_double_arrow_red)
                        setStatus(R.drawable.ic_close)
                    }
                }
            }
        }
        private fun setAction(resourceId: Int) {
            with(itemView) {
                ivAction.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        resourceId
                    )
                )
            }
        }

        private fun setStatus(resourceId: Int) {
            with(itemView) {
                ivStatus.setImageDrawable(ContextCompat.getDrawable(
                    context, resourceId
                ))
            }
        }
    }

    override fun submitList(list: List<Request>?) {
        super.submitList(list?.let { ArrayList(it) })
    }

    class DiffCallback : DiffUtil.ItemCallback<Request>() {
        override fun areContentsTheSame(oldItem: Request, newItem: Request) =
            oldItem == newItem

        override fun areItemsTheSame(oldItem: Request, newItem: Request) =
            oldItem.info.to.email == newItem.info.to.email && oldItem.info.from.email == newItem.info.from.email
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupsViewHolder {
        val item = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_outcoming_request, parent, false)
        return GroupsViewHolder(item)
    }

    override fun onBindViewHolder(holder: GroupsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}