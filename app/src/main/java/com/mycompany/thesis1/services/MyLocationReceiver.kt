package com.mycompany.thesis1.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.LocationResult

class MyLocationReceiver: BroadcastReceiver() {

    override fun onReceive(p0: Context?, intent: Intent?) {
        //Log.d("myTag", "onReceive...")
        if (intent !=null ) {
            val action = intent.action
            if(action==ACTION) {
                val result: LocationResult? = LocationResult.extractResult(intent)
                val location = result?.lastLocation
                //Log.d("myTag", "onReceive: location: latitude:${location?.latitude} && longitude:${location?.longitude}")
            }
        }
    }

    companion object {
        var ACTION = "com.mycompany.thesis1.services.MY_LOCATION_REQUEST_ACTION"
    }
}