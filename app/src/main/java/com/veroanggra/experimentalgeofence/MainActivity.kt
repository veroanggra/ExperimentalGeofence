package com.veroanggra.experimentalgeofence

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.veroanggra.experimentalgeofence.databinding.ActivityMainBinding

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
        TODO("Not yet implemented")
    }
}