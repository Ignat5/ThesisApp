package com.mycompany.thesis1

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.nsd.NsdManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.mycompany.thesis1.models.UserLocation
import com.mycompany.thesis1.services.MyLocationService
import com.mycompany.thesis1.utils.*
import com.mycompany.thesis1.utils.Constants.ACTION_START_SERVICE
import com.mycompany.thesis1.utils.Constants.ACTION_STOP_SERVICE
import kotlinx.android.synthetic.main.fragment_service.*

class ServiceFragment: Fragment(R.layout.fragment_service) {

    var dbListener: ListenerRegistration? = null

    var isAccepted: Boolean = false

    //val args: ServiceFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //val userName = args.userName
        //tvUserName.setText(userName)

        startService.setOnClickListener {
            checkPermissions()
            if(isAccepted) {
                val intent =
                    Intent(activity?.applicationContext, MyLocationService::class.java).apply {
                        action = ACTION_START_SERVICE
                    }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Log.d("myTag", "onViewCreated1: ")
                    activity?.applicationContext?.startForegroundService(intent)
                } else {
                    Log.d("myTag", "onViewCreated2: ")
                    activity?.applicationContext?.startService(intent)
                }
            } else {
                Toast.makeText(context, "Для использования сервиса разрешите отслеживание местоположения", Toast.LENGTH_LONG).show()
                checkPermissions()
            }
        }



        stopService.setOnClickListener {
            val intent = Intent(activity?.applicationContext, MyLocationService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
            activity?.applicationContext?.stopService(intent)
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
                    isAccepted = true
                    //initListeners()
                    //updateLocation()
                    //getLastKnownLocation()
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
            Log.d(TAG, "onRequestPermissionsResult1...")
            if (requestCode == REQUEST_LOCATION) {
                when (afterRequestPermissions(permissions, grantResults)) {
                    AfterRequestPermissionsResult.Granted -> {
                        Log.d(TAG, "onRequestPermissionsResult: permission granted")
                        isAccepted = true

                    }
                    else -> {
                        Log.d(TAG, "onRequestPermissionsResult: permission denied")
                        isAccepted = false
                    }
                }
            } else {
                Log.d(TAG, "onRequestPermissionsResult-else: ")
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }


    @SuppressLint("MissingPermission")
    private fun onLocationAdd() {
        LocationServices.getFusedLocationProviderClient(requireContext()).lastLocation.addOnCompleteListener {
            val location = it.result
            location?.let {
                val db = FirebaseFirestore.getInstance()
                val map = hashMapOf(
                    "latitude" to location.latitude,
                    "longitude" to location.longitude,
                    "name" to "Ned"
                )
                db.collection("TestCollection").document("myId2").set(map).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.d(TAG, "onLocationAdd is successful ")
                    }
                    if (it.isCanceled) {
                        Log.d(TAG, "onLocationAdd is canceled ")
                    }
                }
            }
        }
    }
    @SuppressLint("MissingPermission")
    private fun onLocationUpdate() {
        LocationServices.getFusedLocationProviderClient(requireContext()).lastLocation.addOnCompleteListener {
            val location = it.result
            location?.let {
                val db = FirebaseFirestore.getInstance()
                val map: MutableMap<String, Any> = hashMapOf(
                    "latitude" to location.latitude,
                    "longitude" to location.longitude
                )
                db.collection("TestCollection").document("myId1").update(map).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.d(TAG, "onLocationUpdate is successful ")
                    }
                    if (it.isCanceled) {
                        Log.d(TAG, "onLocationUpdate is canceled ")
                    }
                }
            }
        }
    }

    private fun onReadLocation() {
        val db = FirebaseFirestore.getInstance()
        db.collection("TestCollection").get().addOnCompleteListener {
            if(it.isSuccessful) {
                val result = it.result
                if (result != null) {
                    val documents = result.documents
                    for (document in documents) {
                        val userLocation = document.toObject(UserLocation::class.java)
                        Log.d(TAG, "onReadLocation: $userLocation")
                    }
                }
            }
        }
    }

    private fun onListenStart() {
        val db = FirebaseFirestore.getInstance()
        dbListener = db.collection("TestCollection").addSnapshotListener { value, error ->
            if (error == null) {
//                val documents = value?.documents
//                if (documents != null)
//                for (document in documents) {
//                    val userLocation = document.toObject(UserLocation::class.java)
//                    Log.d(TAG, "onReadLocation: $userLocation")
//                }
                val changes = value?.documentChanges
                if(changes != null)
                for (changedLocation in changes) {
                    val userLocation = changedLocation?.document?.toObject(UserLocation::class.java)
                    Log.d(TAG, "onChangeFixed: $userLocation,changedDocumentID: ${changedLocation?.document?.id} ,changeType: ${changedLocation?.type}")
                }
            }
        }
    }

    private fun onStopListen() {
        if (dbListener != null) {
            dbListener?.remove()
            dbListener = null
        }
    }



    companion object {
        private const val TAG = "myTag"
        private const val REQUEST_LOCATION = 101
    }

}