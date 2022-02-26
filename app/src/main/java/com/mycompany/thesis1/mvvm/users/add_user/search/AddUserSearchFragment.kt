package com.mycompany.thesis1.mvvm.users.add_user.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.mycompany.thesis1.R
import com.mycompany.thesis1.mvvm.model.CommonResource
import com.mycompany.thesis1.mvvm.model.Request
import com.mycompany.thesis1.mvvm.model.User
import kotlinx.android.synthetic.main.fragment_add_user_search.*
import kotlinx.coroutines.flow.collect

class AddUserSearchFragment(val groupId: String): Fragment(R.layout.fragment_add_user_search) {

    private val viewModel by viewModels<AddUserSearchViewModel>()
    private var user: User? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("myTag", "onViewCreated...")
        initViews()
        initDataListeners()
    }

    private fun initDataListeners() {
        viewModel.searchedUser.observe(viewLifecycleOwner) { searchedUser ->
            progressBar.visibility = View.GONE
            if(searchedUser != null) {
                user = searchedUser
                btnSend.visibility = View.VISIBLE
                val text = "${searchedUser!!.userName} (${searchedUser!!.userId})"
                tvSearchResult.text = text
                tvStatus.text = "Пользователь успешно найден"
                btnSearch.isEnabled = false
                btnSearch.setTextColor(ContextCompat.getColor(requireContext(), R.color.white_60))
            } else {
                btnSearch.isEnabled = false
                btnSend.visibility = View.GONE
                btnSearch.setTextColor(ContextCompat.getColor(requireContext(), R.color.white_60))
                tvStatus.text = "Пользователь с введенным email не найден"
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.eventsFlow.collect { resource ->
                when(resource) {
                    is CommonResource.ShowDownload -> progressBar.visibility = View.VISIBLE
                    is CommonResource.Error -> {
                        progressBar.visibility = View.GONE
                        resource.message?.let {
                            Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
                        }
                    }
                    is CommonResource.ShowMessage -> {
                        resource.message?.let {
                            progressBar.visibility = View.GONE
                            btnSend.visibility = View.GONE
                            Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
                        }
                    }
                    is CommonResource.Success -> {
                        resource.message?.let {
                            progressBar.visibility = View.GONE
                            etEmail.clearFocus()
                            Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
                            reset()
                        }
                    }
                }
            }
        }
    }

    private fun initViews() {
        etEmail.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(!etEmail.text.isNullOrBlank()) {
                    btnSearch.isEnabled = true
                    btnSearch.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    tvStatus.text = ""
                } else {
                    btnSearch.isEnabled = false
                    btnSearch.setTextColor(ContextCompat.getColor(requireContext(), R.color.white_60))
                    tvStatus.text = "Поле Email не может быть пустым"
                }
            }

            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })
        btnSearch.setOnClickListener {
            etEmail.text?.let {
                tvSearchResult.text = ""
                btnSend.visibility = View.GONE
                viewModel.searchUser(it.toString())
            }
        }

        btnSend.setOnClickListener {
            user?.let {
                viewModel.sendRequest(it, groupId)
            }
        }
    }

    private fun reset() {
        btnSend.visibility = View.GONE
        etEmail.setText("")
        etEmail.clearFocus()
        tvStatus.text = "Поле Email не может быть пустым"
        btnSearch.isEnabled = false
        btnSearch.setTextColor(ContextCompat.getColor(requireContext(), R.color.white_60))
        tvSearchResult.text = ""
        user = null
    }

    private fun test() {
        val db = FirebaseFirestore.getInstance()
        db.collection("RequestsTest")
            .whereEqualTo("info.from.email","emailFrom1")
            .whereEqualTo("info.to.email","emailTo1")
            .get().addOnCompleteListener { task ->
            try {
                if(task.exception == null) {
                    task.result.documents.let { documents ->
                        for(document in documents) {
                            val request = document.toObject(Request::class.java)
                            Log.d("myTag", "test: $request")
//                            val userName = document["info.to.name"] as String
//                            Log.d("myTag", "test: $userName")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.d("myTag", "test: error: ${e.message}")
            }
        }
    }

    override fun onPause() {
        super.onPause()
    }
}