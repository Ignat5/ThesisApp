package com.mycompany.thesis1

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.algo.GridBasedAlgorithm
import com.google.maps.android.ui.IconGenerator
import com.mycompany.thesis1.models.ClusterMarker
import com.mycompany.thesis1.models.MyClusterRenderer
import com.mycompany.thesis1.models.UserMap
import com.mycompany.thesis1.services.MyLocationReceiver
import com.mycompany.thesis1.utils.AfterRequestPermissionsResult
import com.mycompany.thesis1.utils.BeforeRequestPermissionResult
import com.mycompany.thesis1.utils.afterRequestPermissions
import com.mycompany.thesis1.utils.beforeRequestPermissions
import kotlinx.android.synthetic.main.fragment_map.*

class MapFragment: Fragment(R.layout.fragment_map), OnMapReadyCallback {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap
    private var clusterManager: ClusterManager<ClusterMarker>? = null
    private var clusterRenderer: MyClusterRenderer? = null
    private lateinit var myLocation: Location
    private lateinit var locationRequest: LocationRequest

    var marker: ClusterMarker? = null

    private var markerList: MutableList<ClusterMarker> = mutableListOf()
    private var dbListener: ListenerRegistration? = null

    private var listOfUsers: MutableList<UserMap> = mutableListOf()

    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("myTag", "granted")
            } else {
                Log.d("myTag", "denied")
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) mapViewBundle =
            savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY)
        mapView.onCreate(mapViewBundle)
        mapView.getMapAsync(this)
        checkPermissions()
        ivAction.setOnClickListener {

        }
    }

    private fun stopListenDB() {
        Log.d(TAG, "stopListenDB...")
        if (dbListener != null) {
            clusterManager?.clearItems()
            clusterManager?.cluster()
            markerList.clear()
            dbListener?.remove()
            dbListener = null
        }
    }

    private fun startListenDB() {
        Log.d(TAG, "startListenDB...")
        val db = FirebaseFirestore.getInstance()
//        if (clusterManager == null) {
//            clusterManager = ClusterManager(requireContext(), googleMap)
//            clusterRenderer = MyClusterRenderer(requireContext(), googleMap, clusterManager!!)
//            clusterManager?.renderer = clusterRenderer
//        }
        dbListener = db.collection("TestCollection").addSnapshotListener { value, error ->
            if (error == null) {
                val collectionChanges = value?.documentChanges
                if (collectionChanges != null) {
                    for (documentChange in collectionChanges) {
                        when (documentChange?.type) {
                            DocumentChange.Type.ADDED -> {
                                documentChange.document.let {
                                    val markerOptions = MarkerOptions()
                                    val location = LatLng(it["latitude"] as Double, it["longitude"] as Double)
                                    markerOptions.position(location)
                                    val iconGenerator = IconGenerator(context)
                                    val icon = iconGenerator.makeIcon(it["name"] as String)
                                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title("some title").snippet("some snippet")
                                    val newMarker = googleMap.addMarker(markerOptions)
                                    listOfUsers.add(
                                        UserMap(
                                            id = it.id,
                                            marker = newMarker!!
                                        )
                                    )

//                                    val addedMarker = ClusterMarker(
//                                        userId = it.id,
//                                        latitude = it["latitude"] as Double,
//                                        longitude = it["longitude"] as Double,
//                                        userInitials = it["name"] as String
//                                    )
//                                    markerList.add(addedMarker)
//                                    clusterManager?.addItem(addedMarker)
                                    Log.d(TAG, "startListenDB-ADDED: $listOfUsers")
                                }
                            }
                            DocumentChange.Type.MODIFIED -> {
                                documentChange.document.let {
                                    val updatedUser = listOfUsers.find { user -> user.id == it.id }
                                    updatedUser?.marker?.position = LatLng(it["latitude"] as Double, it["longitude"] as Double)
                                    Log.d(TAG, "startListenDB: marker changed with id: ${updatedUser?.id}, users: $listOfUsers")
//                                    val indexOfChangedMarker =
//                                        markerList.indexOfFirst { marker -> marker.userId == it.id }
//                                    val updatedMarker = ClusterMarker(
//                                        userId = it.id,
//                                        latitude = it["latitude"] as Double,
//                                        longitude = it["longitude"] as Double,
//                                        userInitials = it["name"] as String)
//                                    markerList[indexOfChangedMarker].apply {
//                                        location = LatLng(it["latitude"] as Double, it["longitude"] as Double)
//                                    }
//                                    clusterManager?.clearItems()
//                                    clusterManager?.cluster()
//                                    clusterManager?.addItems(markerList)
//                                    clusterManager?.let { manager ->
//                                        val wasRemoved = manager.removeItem(markerList[indexOfChangedMarker])
//                                        manager.addItem(updatedMarker)
//                                        Log.d(TAG, "startListenDB-MODIFIED: wasRemoved: $wasRemoved,list: $markerList")
//                                    }

//                                    markerList[indexOfChangedMarker].apply {
//                                        location = LatLng(it["latitude"] as Double, it["longitude"] as Double)
//                                    }
//                                    val isUpdated = clusterManager?.updateItem(markerList[indexOfChangedMarker])
//                                    Log.d(TAG, "startListenDB-MODIFIED: isUpdated: $isUpdated")

//                                        clusterRenderer = MyClusterRenderer(requireContext(), googleMap, clusterManager!!)
//                                        clusterManager?.renderer = clusterRenderer
//
//                                    markerList[indexOfChangedMarker] = updatedMarker
//                                    clusterManager?.clearItems()
//                                    //googleMap.clear()
//                                    clusterManager?.addItems(markerList)
//                                    Log.d(TAG, "startListenDB-addItems: $markerList")
//                                    Log.d(TAG, "startListenDB-addItems: ${clusterManager?.markerCollection}")
//                                    clusterManager?.setAnimation(false)

//                                    markerList[indexOfChangedMarker].apply {
//                                        location = LatLng(it["latitude"] as Double, it["longitude"] as Double)
//                                    }
//                                    val isUpdated = clusterManager?.updateItem(markerList[indexOfChangedMarker])
//                                    Log.d(TAG, "startListenDB-MODIFIED: isUpdated: $isUpdated")
//                                    markerList[indexOfChangedMarker] = ClusterMarker(
//                                        userId = it.id,
//                                        latitude = it["latitude"] as Double,
//                                        longitude = it["longitude"] as Double,
//                                        userInitials = it["name"] as String
//                                    )
//                                    val isUpdated = clusterManager?.updateItem(markerList[indexOfChangedMarker])
//                                    Log.d(TAG, "startListenDB-MODIFIED: isUpdated: $isUpdated")
//                                    val updatedMarker = ClusterMarker(
//                                        userId = it.id,
//                                        latitude = it["latitude"] as Double,
//                                        longitude = it["longitude"] as Double,
//                                        userInitials = it["name"] as String
//                                    )
//                                    Log.d(TAG, "startListenDB-MODIFIED: $updatedMarker")
//                                    clusterManager?.let { manager ->
//                                        //manager.removeItem(markerList[indexOfChangedMarker])
//                                        //manager.addItem(updatedMarker)
//                                    }
//                                    clusterRenderer?.setUpdateMarker(updatedMarker)
//                                    markerList[indexOfChangedMarker] = updatedMarker
                                }
                            }
                            DocumentChange.Type.REMOVED -> {
//                                documentChange.document.let {
//                                    val indexOfRemovedMarker =
//                                        markerList.indexOfFirst { marker -> marker.userId == it.id }
//                                    clusterManager?.removeItem(markerList[indexOfRemovedMarker])
//                                    markerList.removeAt(indexOfRemovedMarker)
//                                    Log.d(TAG, "startListenDB-REMOVED: $markerList")
//                                }
                            }
                        }
                    }
                    //clusterManager?.cluster()
                }
            } else Log.d(TAG, "startListenDB: error")
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateLocation() {
        Log.d("myTag", "updateLocation: ")
        if (clusterManager == null) {
            clusterManager = ClusterManager(requireContext(), googleMap)
            clusterRenderer = MyClusterRenderer(requireContext(), googleMap, clusterManager!!)
            clusterManager?.renderer = clusterRenderer
        }
        val location = LatLng(50.0,50.0)
        marker = ClusterMarker(location.latitude,location.longitude,
            "Title (You)","snippet(You)","IL",R.color.design_default_color_secondary)
        clusterManager?.addItem(marker)
        val cameraPosition = CameraPosition.Builder()
            .target(LatLng(location.latitude, location.longitude))
            .zoom(15f)
            .build()
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),1000,null)

//        buildLocationRequest()
//        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
//        fusedLocationProviderClient.requestLocationUpdates(locationRequest,getPendingIntent())
    }

    private fun updateMarker() {
        Log.d(TAG, "updateMarker...")
        clusterManager?.removeItem(marker)
        val location = LatLng(marker?.position?.latitude?.plus(0.1)!!,51.0)
        marker = ClusterMarker(location.latitude,location.longitude,
            "Title (You)","snippet(You)","IL_updated",R.color.design_default_color_secondary)
        clusterManager?.addItem(marker)
        clusterManager?.cluster()
        val cameraPosition = CameraPosition.Builder()
            .target(LatLng(location.latitude, location.longitude))
            .zoom(15f)
            .build()
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),1000,null)
    }

    private fun getPendingIntent(): PendingIntent {
        val intent = Intent(requireContext(), MyLocationReceiver::class.java)
        intent.action = MyLocationReceiver.ACTION
        return PendingIntent.getBroadcast(requireContext(),0,intent,PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun buildLocationRequest() {
        locationRequest = LocationRequest.create()
            .setInterval(5000L)
            .setSmallestDisplacement(10f)
            .setFastestInterval(5000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
    }

    private fun addMarkers(clusterMarkerList: List<ClusterMarker>) {
           if (clusterManager == null) {
               clusterManager = ClusterManager(requireContext(), googleMap)
               clusterRenderer = MyClusterRenderer(requireContext(), googleMap, clusterManager!!)
               clusterManager?.renderer = clusterRenderer
           }
        clusterManager?.addItems(clusterMarkerList)
        clusterManager?.cluster()
    }

    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val location = task.result
                myLocation = location
                addMarkers(arrayListOf(ClusterMarker(location.latitude,location.longitude,
                    "Title (You)","snippet(You)","IL",R.color.design_default_color_secondary),
                    ClusterMarker(location.latitude + 0.1,location.longitude + 0.1,
                        "Title (Not You)","snippet(Not You)","SE",R.color.white))
                )
                val cameraPosition = CameraPosition.Builder()
                    .target(LatLng(location.latitude, location.longitude))
                    .zoom(15f)
                    .build()
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),3000,null)
            } else {
                Log.d(TAG, "getLastKnownLocation: task is NOT successful")
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun initListeners() {
        startListen.setOnClickListener { startListenDB() }
        stopListen.setOnClickListener { stopListenDB() }
        ivMyLocation.setOnClickListener {
//            updateMarker()
//            fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    val location = task.result
//                    Log.d(TAG, "getLastKnownLocation: your position is: latitude-${location.latitude} && longitude=${location.longitude}")
//                    val cameraPosition = CameraPosition.Builder()
//                        .target(LatLng(location.latitude, location.longitude))
//                        .zoom(10f)
//                        .build()
//                    googleMap.animateCamera(
//                        CameraUpdateFactory.newCameraPosition(cameraPosition),
//                        2000,
//                        null
//                    )
//                } else {
//                    Log.d(TAG, "getLastKnownLocation: task is NOT successful")
//                }
//            }
        }

        /*ivPanorama.setOnClickListener {
            addMarkers(arrayListOf(ClusterMarker(myLocation.latitude-0.1,myLocation.longitude-0.1,
                "Title (You)","snippet(You)","IL",R.color.design_default_color_secondary),
                ClusterMarker(myLocation.latitude + 0.2,myLocation.longitude + 0.2,
                    "Title (Not You)","snippet(Not You)","SE",R.color.white))
            )
        }*/
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
                    initListeners()
                    updateLocation()
                    //getLastKnownLocation()
                }
                else -> {
                    //requestPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    Log.d(TAG, "onRequestPermissionsResult: permission denied")
                }
            }
        } else {
            Log.d(TAG, "onRequestPermissionsResult-else: ")
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onMapReady(p0: GoogleMap) {
        Log.d(TAG, "onMapReady: ")
        googleMap = p0
        googleMap.uiSettings.isMapToolbarEnabled = false
        googleMap.uiSettings.isCompassEnabled = false
        p0.addMarker(MarkerOptions().position(LatLng(0.0,0.0)).title("some title"))
        initListeners()
        //updateLocation()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
        dbListener?.remove()
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        if (mapView != null) mapView.onDestroy()
//    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle)
        }
        mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    companion object {
        private val MAPVIEW_BUNDLE_KEY = "MAPVIEW_BUNDLE_KEY"
        private const val TAG = "myTag"
        private const val REQUEST_LOCATION = 101
    }

}