package com.mycompany.thesis1.mvvm.requests.active

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.mycompany.thesis1.R
import com.mycompany.thesis1.mvvm.model.CommonResource
import com.mycompany.thesis1.mvvm.model.Request
import kotlinx.android.synthetic.main.fragment_group_users.*
import kotlinx.android.synthetic.main.fragment_incoming_requests.*
import kotlinx.android.synthetic.main.fragment_incoming_requests.progressBar
import kotlinx.coroutines.flow.collect

class IncomingRequestsFragment : Fragment(R.layout.fragment_incoming_requests) {

    private val viewModel by viewModels<IncomingRequestsViewModel>()
    private var adapter: IncomingListAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initDataListeners()
    }

    private fun initDataListeners() {
        viewModel.incomingRequests.observe(viewLifecycleOwner) { requests ->
            progressBar.visibility = View.GONE
            if (requests.isEmpty())
                tvMessage.visibility = View.VISIBLE
            else
                tvMessage.visibility = View.GONE
            adapter?.submitList(requests)
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.eventsFlow.collect { resource ->
                when (resource) {
                    is CommonResource.ShowDownload -> progressBar.visibility = View.VISIBLE
                    is CommonResource.Error -> {
                        Log.d("myTag", "initDataListeners: error: ${resource.message}")
                        progressBar.visibility = View.GONE
                        resource.message?.let {
                            Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
                        }
                    }
                    is CommonResource.ShowMessage -> {
                        progressBar.visibility = View.GONE
                        resource.message?.let {
                            Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
                        }
                    }
                    is CommonResource.Success -> {
                        progressBar.visibility = View.GONE
                        resource.message?.let {
                            Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun initRecyclerView() {
        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        rvIncomingRequests.layoutManager = linearLayoutManager
        adapter = IncomingListAdapter(object : IncomingListAdapter.Listener {
            override fun onAcceptClick(request: Request) {
                showAcceptDialog(request) {
                    viewModel.onRequestAccepted(request)
                }
            }

            override fun onDenyClick(request: Request) {
                showDenyDialog(request) {
                    viewModel.onRequestDenied(request)
                }
            }

            override fun onCancelAccepted(request: Request) {
                showDenyDialog(request) {
                    viewModel.onCancelAccepted(request)
                }
            }

            override fun onCancelDenied(request: Request) {
                showAcceptDialog(request) {
                    viewModel.onRequestAccepted(request)
                }
            }
        })
        rvIncomingRequests.adapter = adapter
    }

    private fun showAcceptDialog(request: Request, callback: (() -> Unit)) {
        MaterialAlertDialogBuilder(
            requireContext(),
            R.style.MyThemeOverlay_MaterialComponents_MaterialAlertDialog
        )
            .setTitle("Подтверждение")
            .setMessage("Вы действительно хотите принять запрос от пользователя ${request.info.from.name}")
            .setPositiveButton("Да") { _, _ ->
                callback.invoke()
            }
            .setNegativeButton("Нет") { _, _ -> }
            .show()
    }

    private fun showDenyDialog(request: Request, callback: (() -> Unit)) {
        MaterialAlertDialogBuilder(
            requireContext(),
            R.style.MyThemeOverlay_MaterialComponents_MaterialAlertDialog
        )
            .setTitle("Подтверждение")
            .setMessage("Вы действительно хотите отклонить запрос от пользователя ${request.info.from.name}")
            .setPositiveButton("Да") { _, _ ->
                callback.invoke()
            }
            .setNegativeButton("Нет") { _, _ -> }
            .show()
    }

}