package com.mycompany.thesis1.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log

class LocationHelper {
    private var LOCATION_REFRESH_TIME = 3000 // 3 seconds. The Minimum Time to get location update
    private var LOCATION_REFRESH_DISTANCE = 0 // 0 meters. The Minimum Distance to be changed to get location update
    private lateinit var mLocationManager: LocationManager
    private lateinit var locationListener: LocationListener

    @SuppressLint("MissingPermission")
    fun startListeningUserLocation(context: Context, myListener: MyLocationListener) {
        Log.d("myTag", "startListeningUserLocation...")
        mLocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                myListener.onLocationChanged(location) // calling listener to inform that updated location is available
            }
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        }
        mLocationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            LOCATION_REFRESH_TIME.toLong(),
            LOCATION_REFRESH_DISTANCE.toFloat(),
            locationListener
        )
    }

    fun stopListeningUserLocation() {
        Log.d("myTag", "stopListeningUserLocation...")
        mLocationManager.removeUpdates(locationListener)
    }
}

interface MyLocationListener {
    fun onLocationChanged(location: Location?)
}