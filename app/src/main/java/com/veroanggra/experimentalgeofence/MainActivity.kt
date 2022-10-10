package com.veroanggra.experimentalgeofence

import android.Manifest
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.veroanggra.experimentalgeofence.databinding.ActivityMainBinding
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

    override fun onMapReady(p0: GoogleMap) {
        map = p0
        map.uiSettings.isZoomControlsEnabled = true

        if (!PermissionHelper.hasPermission(this, locationPermissions)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                locationPermissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
            ActivityCompat.requestPermissions(this, locationPermissions.toTypedArray(), LOCATION_REQUEST_CODE)
        } else {
            if (PermissionHelper.getDeniedPermission(this, locationPermissions)) {
                return
            }
        }
    }

    companion object {
        val locationPermissions = mutableListOf<String>(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        const val LOCATION_REQUEST_CODE = 123
    }
}