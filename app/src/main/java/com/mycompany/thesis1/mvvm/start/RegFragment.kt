package com.mycompany.thesis1.mvvm.start

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.mycompany.thesis1.R
import kotlinx.android.synthetic.main.fragment_registration_mvvm.*
import kotlinx.android.synthetic.main.layout_toolbar.*
import kotlinx.coroutines.flow.collect
import java.text.SimpleDateFormat
import java.util.*

class RegFragment: Fragment(R.layout.fragment_registration_mvvm) {

    private val viewModel by viewModels<RegViewModel>()
    private var locationCallback: LocationCallback = object: LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)
            val location = p0.lastLocation
            Log.d("myTag", "onLocationResult: ${location.longitude}, ${location.latitude}")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvScreenTitle.text = "Регистрация"
        initListeners()
    }

    @SuppressLint("MissingPermission")
    private fun initListeners() {
        ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
        btnRegister.setOnClickListener {
            hideKeyboard()
            viewModel.onRegisterClick(
                name = etName.text.toString(),
                email = etEmail.text.toString(),
                password = etPassword.text.toString()
            )
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.registrationEventFlow.collect { event ->
                when(event) {
                    is RegViewModel.RegistrationEvents.ShowLoading -> {
                        Log.d("myTag", "ShowLoading: ")
                        pbRegistration.visibility = View.VISIBLE
                    }
                    is RegViewModel.RegistrationEvents.NavigateToMainScreen -> {
                        Log.d("myTag", "NavigateToMainScreen: ")
                        pbRegistration.visibility = View.GONE
                        etPassword.clearFocus()
                        etEmail.clearFocus()
                        etName.clearFocus()
                        val action = RegFragmentDirections.actionRegFragmentToMainFragment()
                        findNavController().navigate(action)
                    }
                    is RegViewModel.RegistrationEvents.ShowErrorMessage -> {
                        Log.d("myTag", "ShowErrorMessage: ")
                        pbRegistration.visibility = View.GONE
                        Snackbar.make(requireView(), event.message, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }

    }

    override fun onStop() {
        super.onStop()
        LocationServices.getFusedLocationProviderClient(requireContext()).removeLocationUpdates(locationCallback)
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = activity?.currentFocus
        if(view == null)
            view = View(requireActivity())
        imm.hideSoftInputFromWindow(view.windowToken,0)
    }

}