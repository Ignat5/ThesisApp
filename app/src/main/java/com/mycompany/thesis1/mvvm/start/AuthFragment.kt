package com.mycompany.thesis1.mvvm.start

import android.Manifest
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.mycompany.thesis1.R
import com.mycompany.thesis1.utils.AfterRequestPermissionsResult
import com.mycompany.thesis1.utils.BeforeRequestPermissionResult
import com.mycompany.thesis1.utils.afterRequestPermissions
import com.mycompany.thesis1.utils.beforeRequestPermissions
import kotlinx.android.synthetic.main.fragment_auth_mvvm.*
import kotlinx.android.synthetic.main.layout_toolbar.*
import kotlinx.coroutines.flow.collect

class AuthFragment : Fragment(R.layout.fragment_auth_mvvm) {

    private val viewModel by viewModels<AuthViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        tvScreenTitle.text = "Авторизация"
        ivBack.visibility = View.GONE
        initListeners()
    }

    private fun initListeners() {
        btnAuth.setOnClickListener {
            hideKeyboard()
            viewModel.onAuthenticateClick(
                email = etEmail.text.toString(),
                password = etPassword.text.toString()
            )
        }

        tvRegistration.setOnClickListener {
            viewModel.onRegistrationClick()
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.authFlow.collect { event ->
                when (event) {
                    is AuthViewModel.AuthEvents.NavigateToMainScreen -> {
                        pbAuth.visibility = View.GONE
                        val action = AuthFragmentDirections.actionAuthFragment2ToMainFragment()
                        findNavController().navigate(action)
                    }
                    is AuthViewModel.AuthEvents.ShowLoading -> pbAuth.visibility = View.VISIBLE

                    is AuthViewModel.AuthEvents.ShowErrorMessage -> {
                        pbAuth.visibility = View.GONE
                        Snackbar.make(requireView(), event.message, Snackbar.LENGTH_LONG).show()
                    }
                    is AuthViewModel.AuthEvents.NavigateToRegistrationScreen -> {
                        pbAuth.visibility = View.GONE
                        val action = AuthFragmentDirections.actionAuthFragment2ToRegFragment()
                        findNavController().navigate(action)
                    }
                    is AuthViewModel.AuthEvents.CheckPermissions -> checkPermissions()
                }
            }
        }
    }

    private fun checkPermissions() {
        Log.d(TAG, "checkPermissions...")
        val locationPermissionsState = beforeRequestPermissions(
            REQUEST_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
        when (locationPermissionsState) {
            BeforeRequestPermissionResult.AlreadyGranted -> {
                Log.d(TAG, "checkPermissions: permission is already granted")
            }
            BeforeRequestPermissionResult.ShowRationale -> {
                beforeRequestPermissions(
                    REQUEST_LOCATION, true,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                )
            }
            BeforeRequestPermissionResult.Requested -> Unit
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermissionsResult...")
        if (requestCode == REQUEST_LOCATION) {
            when (afterRequestPermissions(permissions, grantResults)) {
                AfterRequestPermissionsResult.Granted -> {
                    Log.d(TAG, "onRequestPermissionsResult: permission granted")
                    //viewModel.onPermissionsGranted()
                }
                AfterRequestPermissionsResult.Denied -> {
                    Log.d(TAG, "onRequestPermissionsResult: Denied")
                }
                AfterRequestPermissionsResult.NeverAskAgain -> {
                    Log.d(TAG, "onRequestPermissionsResult: NeverAskAgain")
                }
            }
        } else {
            Log.d(TAG, "onRequestPermissionsResult-else: ")
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = activity?.currentFocus
        if (view == null)
            view = View(requireActivity())
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    companion object {
        private const val TAG = "myTag"
        private const val REQUEST_LOCATION = 101
    }
}