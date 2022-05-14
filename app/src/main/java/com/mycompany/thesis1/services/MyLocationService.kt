package com.mycompany.thesis1.services

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.*
import android.util.Log
import androidx.core.R
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mycompany.thesis1.constants.AppConstants.LATITUDE
import com.mycompany.thesis1.constants.AppConstants.LONGITUDE
import com.mycompany.thesis1.constants.AppConstants.UPDATED_AT
import com.mycompany.thesis1.utils.Constants.ACTION_START_SERVICE
import com.mycompany.thesis1.utils.Constants.ACTION_STOP_SERVICE
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.lang.Exception
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

class MyLocationService: Service() {
    private var locationRequest: LocationRequest? = null
    private var isRunning = false
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val sdf = SimpleDateFormat("dd.MM HH:mm:ss")
    private var locationCallback = object: LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult?.let { super.onLocationResult(it) }
            if(locationResult!= null) {
                val latitude = locationResult.lastLocation.latitude
                val longitude = locationResult.lastLocation.longitude
                Log.d(TAG, "onLocationResult: latitude: $latitude && longitude: $longitude")
                val db = FirebaseFirestore.getInstance()
                val map = mutableMapOf<String, Any>(
                    LATITUDE to latitude,
                    LONGITUDE to longitude,
                    UPDATED_AT to Timestamp(Date())
                )
                firebaseAuth.currentUser?.email?.let { userId->
                    db.collection("UsersTest").document(userId).update(map).addOnCompleteListener {
                        if(it.isSuccessful) {
                            getDisplacementFixed(locationResult.lastLocation)
                            counter++
//                            if (counter == 0) {
//                                lastLocation = locationResult.lastLocation
//                                Log.d(TAG, "onLocationResult: counter == 0, lastLocationLat: ${lastLocation?.latitude}")
//                            } else {
//                                lastLocation?.let { location ->
////                                    getDisplacement(location, locationResult.lastLocation)
////                                    getDisplacementFixed(locationResult.lastLocation)
//                                    lastLocation = locationResult.lastLocation
//                                }
//                            }
                        } else {
                            Log.d(TAG, "onLocationResult-failure")
                        }
                    }
                }
            }
        }
    }

    private fun getDisplacement(oldLocation: Location, newLocation: Location) {
        val fi1 = oldLocation.latitude * Math.PI/180
        val fi2 = newLocation.latitude * Math.PI/180
        val deltaF = (newLocation.latitude - oldLocation.latitude) * Math.PI/180
        val deltaL = (newLocation.longitude - oldLocation.longitude) * Math.PI/180
        val a = sin(deltaF/2) * sin(deltaF/2) + cos(fi1) * cos(fi2) * sin(deltaL/2) * sin(deltaL/2)
        val c = 2 * atan2(sqrt(a), sqrt(1-a))
        val displacement = Radius * c//acos(sin(fi1) * sin(fi2) + cos(fi1) * cos(fi2) * cos(deltaL)) * Radius
        Log.d(TAG, "getDisplacement: displacement: $displacement meters, old: longitude: ${oldLocation.longitude}, latitude: ${oldLocation.latitude}; new: longitude: ${newLocation.longitude}, latitude: ${newLocation.latitude}")
    }

    private fun getDisplacementFixed(newLocation: Location) {
        val homeLatitude = 55.8877764
        val homeLongitude = 37.5239292
        val fi1 = homeLatitude * Math.PI/180
        val fi2 = newLocation.latitude * Math.PI/180
        val deltaF = (newLocation.latitude - homeLatitude) * Math.PI/180
        val deltaL = (newLocation.longitude - homeLongitude) * Math.PI/180
        val a = sin(deltaF/2) * sin(deltaF/2) + cos(fi1) * cos(fi2) * sin(deltaL/2) * sin(deltaL/2)
        val c = 2 * atan2(sqrt(a), sqrt(1-a))
        val displacement = Radius * c
        val updateTime = sdf.format(Date())
        writeToFile(
            "counter: $counter \n displacement: $displacement meters \n time: $updateTime \n <==========> \n\n"
        )
        Log.d(TAG, "getDisplacement: \n counter: $counter \n displacement: $displacement meters \n time: $updateTime \n <=========>\n" +
                "\n")
    }

    private fun writeToFile(info: String) {
        try {
//            val root = File(applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString())
//            val textFile = File(root, "ThesisStatistics1.txt")
            val root = File(applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString())
            this.baseContext
            val textFile = File(root, "ThesisStatistics.txt")

            val writer = BufferedWriter(FileWriter(textFile, true))

            writer.append(info)
            writer.flush()
            writer.close()

//            val writer = FileWriter(textFile)
//            writer.write(info)
//            applicationContext.openFileOutput(textFile.absolutePath, MODE_APPEND).use { output ->
//                info.byteInputStream().copyTo(output)
//            }
//            FileOutputStream(textFile, true).use { output ->
//                info.byteInputStream().copyTo(output)
//            }
//            writer.append(info)
//            writer.flush()
//            writer.close()
            Log.d("myTag2", "writeToFile...OK")
        } catch (e: Exception) {
            Log.d("myTag2", "writeToFile...ERROR: ${e.message}")
        }
    }



    private fun createNotification(latitude: Double, longitude: Double): Notification {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Местоположение отслеживается...")
            .setContentText("Ваша широта: $latitude, долгота: $longitude")
            .setChannelId(CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_template_icon_bg)
            .setPriority(NotificationManager.IMPORTANCE_LOW)
        return builder.build()
    }

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_SERVICE -> {
                if (!isRunning) {
                    Log.d(TAG, "onStartCommand: service started")
                    isRunning = true
                } else {
                    Log.d(TAG, "onStartCommand: service already started")
//                    removeLocationListener()
//                    startLocationListener()
                }
            }
            ACTION_STOP_SERVICE -> {
                if (isRunning) {
                    Log.d(TAG, "onStartCommand: stopSelf()")
                    removeLocationListener()
                    stopForeground(true)
                    stopSelf()
                } else {
                    Log.d(TAG, "onStartCommand: service already stopped")
                }
            }
        }
        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreateService...")
        removeLocationListener()
        startLocationListener()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationListener() {
        Log.d(TAG, "startLocationListener...")
        createNotificationChannel()
        locationRequest = LocationRequest.create()
        locationRequest?.apply {
            interval = 5000
            priority = PRIORITY_HIGH_ACCURACY
            fastestInterval = 3000
            smallestDisplacement = 10f
        }
            LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest!!, locationCallback, Looper.getMainLooper())
            startForeground(1, createNotification())
    }

    private fun removeLocationListener() {
        Log.d(TAG, "removeLocationListener...")
        LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(locationCallback)
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        Log.d(TAG, "onTaskRemoved...")
        removeLocationListener()
        startLocationListener()
        val restartServiceIntent = Intent(applicationContext, MyLocationService::class.java).also {
            it.setPackage(packageName)
        };
        val restartServicePendingIntent: PendingIntent = PendingIntent.getService(this, 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_MUTABLE);
        applicationContext.getSystemService(Context.ALARM_SERVICE);
        val alarmService: AlarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager;
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 3000, restartServicePendingIntent);
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: service destroyed")
        removeLocationListener()
//        val restartServiceIntent = Intent(applicationContext, MyLocationService::class.java).also {
//            it.setPackage(packageName)
//        };
//        val restartServicePendingIntent: PendingIntent = PendingIntent.getService(this, 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
//        applicationContext.getSystemService(Context.ALARM_SERVICE);
//        val alarmService: AlarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager;
//        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 5000, restartServicePendingIntent);
        //locationHelper.stopListeningUserLocation()
        //fusedLocationProviderClient.removeLocationUpdates(callback)
    }

    override fun stopService(name: Intent?): Boolean {
        Log.d(TAG, "stopService: service stopped")
        return super.stopService(name)
    }

    private fun createNotification(): Notification {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentText("Ваше местоположение отслеживается")
//            .setContentTitle("")
            .setChannelId(CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_template_icon_bg)
            .setPriority(NotificationManager.IMPORTANCE_LOW)
            return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = CHANNEL_NAME
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID,name,importance)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private var counter = 0
        private var lastLocation: Location? = null
        private const val Radius = 6371000
        private val TAG = "myTag1"
        private val CHANNEL_NAME = "MY_CHANNEL_NAME"
        private val CHANNEL_ID = "com.mycompany.thesis1.services.MyService"
    }

}