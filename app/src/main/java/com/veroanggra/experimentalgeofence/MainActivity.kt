package com.veroanggra.experimentalgeofence

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.veroanggra.experimentalgeofence.databinding.ActivityMainBinding
import com.veroanggra.experimentalgeofence.util.Campaign
import com.veroanggra.experimentalgeofence.util.PermissionHelper

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

    @SuppressLint("MissingPermission")
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
            if (PermissionHelper.getDeniedPermission(this, locationPermissions)) {
                return
            }
            this.map.isMyLocationEnabled = true
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
    }
}