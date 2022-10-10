package com.veroanggra.experimentalgeofence

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.veroanggra.experimentalgeofence.databinding.ActivityMainBinding
import com.veroanggra.experimentalgeofence.util.Campaign
import com.veroanggra.experimentalgeofence.util.PermissionHelper
import kotlin.random.Random

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMainBinding
    private lateinit var map: GoogleMap
    private lateinit var geoFencingClient: GeofencingClient
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        geoFencingClient = LocationServices.getGeofencingClient(this)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_view)
        setContentView(binding.root)
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        checkPermissionsStartgeofencing()
    }

    private fun checkPermissionsStartgeofencing() {
        TODO("Not yet implemented")
    }

    override fun onMapReady(p0: GoogleMap) {
        map = p0
        map.uiSettings.isZoomControlsEnabled = true

        if (!PermissionHelper.hasPermission(this, locationPermissions)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                locationPermissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
            ActivityCompat.requestPermissions(
                this,
                locationPermissions.toTypedArray(),
                LOCATION_REQUEST_CODE
            )
        } else {
            this.map.isMyLocationEnabled = true
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                if (it != null) {
                    with(map) {
                        val latLng = LatLng(it.latitude, it.longitude)
                        moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, CAMERA_ZOOM_LEVEL))
                    }
                } else {
                    with(map) {
                        moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(65.01355297927051, 25.464019811372978), CAMERA_ZOOM_LEVEL
                            )
                        )
                    }
                }
            }
        }

        setLongClick(map)
        setPointClick(map)
    }

    private fun setPointClick(map: GoogleMap) {
        TODO("Not yet implemented")
    }

    private fun setLongClick(map: GoogleMap) {
        map.setOnMapClickListener { latlng ->
            map.addMarker(
                MarkerOptions().position(latlng)
                    .title("Current Location")
            )?.showInfoWindow()
            map.addCircle(
                CircleOptions()
                    .center(latlng)
                    .strokeColor(Color.argb(50, 70, 70, 70))
                    .fillColor(Color.argb(70, 150, 150, 150))
                    .radius(GEOFENCE_RADIUS.toDouble())
            )
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, CAMERA_ZOOM_LEVEL))
            val database = Firebase.database
            val reference = database.getReference("campaigns")
            val key = reference.push().key
            if (key != null) {
                val campaign = Campaign(key, latlng.latitude, latlng.longitude)
                reference.child(key).setValue(campaign)
            }
            createGeofece(latlng, key, geoFencingClient)
        }

    }

    private fun createGeofece(latlng: LatLng, key: String?, geoFencingClient: GeofencingClient) {
        val geofence = Geofence.Builder()
            .setRequestId(GEOFENCE_ID)
            .setCircularRegion(latlng.latitude, latlng.longitude, GEOFENCE_RADIUS.toFloat())
            .setExpirationDuration(GEOFENCE_EXPIRATION.toLong())
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL)
            .setLoiteringDelay(GEOFENCE_DWELL_DELAY)
            .build()

        val geofenceRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

//        val intent = Intent(this, GEo)
    }

    fun scheduleJob() {
        val componentName = ComponentName(this, CampaignService::class.java)
        val info = JobInfo.Builder(321, componentName)
            .setRequiresCharging(false)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .setPersisted(true)
            .setPeriodic(15 * 60 * 1000)
            .build()

        val scheduler = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
        val resultCode = scheduler.schedule(info)
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.d(TAG, "Job scheduled")
        } else {
            Log.d(TAG, "Job scheduling failed")
            scheduleJob()
        }
    }

    fun cancelJob() {
        val scheduler = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
        scheduler.cancel(321)
        Log.d(TAG, "Job cancelled")
    }

    @SuppressLint("ObsoleteSdkInt")
    fun showNotification(context: Context?, message: String) {
        val CHANNEL_ID = "CAMPAIGN_NOTIFICATION_CHANNEL"
        var notification_id = 1589
        notification_id += Random(notification_id).nextInt(1, 30)

        val notificationBuilder =
            context?.let {
                NotificationCompat.Builder(it.applicationContext, CHANNEL_ID)
                    .setSmallIcon(R.drawable.droid)
                    .setContentTitle(context.getString(R.string.geofencing_title_campaign))
                    .setContentText(message)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT) }

        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, context.getString(R.string.geofencing_title_campaign), NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = context.getString(R.string.geofencing_title_campaign)
            }
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(notification_id, notificationBuilder?.build())
    }

    companion object {
        val locationPermissions = mutableListOf<String>(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        const val LOCATION_REQUEST_CODE = 123
        const val CAMERA_ZOOM_LEVEL = 13f
        const val GEOFENCE_RADIUS = 200
        const val GEOFENCE_ID = "CAMPAIGN_GEOFENCE_ID"
        const val GEOFENCE_EXPIRATION = 10 * 24 * 60 * 60 * 1000 // 10 days
        const val GEOFENCE_DWELL_DELAY = 10 * 1000 // 10 secs // 2 minutes
        private val TAG: String = MainActivity::class.java.simpleName
    }
}