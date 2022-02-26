package com.mycompany.thesis1.mvvm.requests.active

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
import com.mycompany.thesis1.constants.AppConstants.REQUEST_ACCEPTED
import com.mycompany.thesis1.constants.AppConstants.REQUEST_DENIED
import com.mycompany.thesis1.constants.AppConstants.REQUEST_WAITING
import com.mycompany.thesis1.mvvm.model.Request
import kotlinx.android.synthetic.main.item_incoming_request.view.*

class IncomingListAdapter(val listener: Listener) :
    ListAdapter<Request, IncomingListAdapter.GroupsViewHolder>(DiffCallback()) {

    interface Listener {
        fun onAcceptClick(request: Request)
        fun onDenyClick(request: Request)
        fun onCancelAccepted(request: Request)
        fun onCancelDenied(request: Request)
    }

    inner class GroupsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(request: Request) {
            with(itemView) {
                request.let {
                    it.info.from.let { info ->
                        tvUserName.text = info.name
                        tvUserEmail.text = info.email
                    }
                    when (it.status) {
                        REQUEST_ACCEPTED -> {
                            ivOptions.visibility = View.VISIBLE
                            ivAccept.visibility = View.GONE
                            ivDecline.visibility = View.GONE
                            ivAction.setImageDrawable(
                                ContextCompat.getDrawable(
                                    context,
                                    R.drawable.ic_double_arrow_green
                                )
                            )
                        }
                        REQUEST_WAITING -> {
                            ivAccept.visibility = View.VISIBLE
                            ivDecline.visibility = View.VISIBLE
                            ivOptions.visibility = View.GONE
                            ivAction.setImageDrawable(
                                ContextCompat.getDrawable(
                                    context,
                                    R.drawable.ic_double_arrow_black
                                )
                            )

                        }
                        REQUEST_DENIED -> {
                            ivOptions.visibility = View.VISIBLE
                            ivAccept.visibility = View.GONE
                            ivDecline.visibility = View.GONE
                            ivAction.setImageDrawable(
                                ContextCompat.getDrawable(
                                    context,
                                    R.drawable.ic_double_arrow_red
                                )
                            )
                        }
                    }
                }
                ivAccept.setOnClickListener { listener.onAcceptClick(request) }
                ivDecline.setOnClickListener { listener.onDenyClick(request) }
                ivOptions.setOnClickListener {
                    val popup = PopupMenu(itemView.context, clRoot, Gravity.END)
                    if (request.status == REQUEST_ACCEPTED)
                        popup.menuInflater.inflate(
                            R.menu.request_incoming_accepted_menu,
                            popup.menu
                        )
                    else
                        popup.menuInflater.inflate(R.menu.request_incoming_denied_menu, popup.menu)
                    popup.setOnMenuItemClickListener { menuItem ->
                        when (menuItem?.itemId) {
                            R.id.miCancelAccepted ->
                                listener.onCancelAccepted(request)
                            R.id.miCancelDenied ->
                                listener.onCancelDenied(request)
                        }
                        false
                    }
                    popup.show()
                }
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
            .inflate(R.layout.item_incoming_request, parent, false)
        return GroupsViewHolder(item)
    }

    override fun onBindViewHolder(holder: GroupsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}