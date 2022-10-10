package com.veroanggra.experimentalgeofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.veroanggra.experimentalgeofence.util.Campaign

class GeofenceReceiver: BroadcastReceiver() {
    private lateinit var key : String
    private lateinit var text: String

    override fun onReceive(p0: Context?, p1: Intent?) {
        if (p0 != null) {
            val geofencingEvent = p1?.let { GeofencingEvent.fromIntent(it) }
            val geofencingTransition = geofencingEvent?.geofenceTransition

            if (geofencingTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofencingTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
                key = p1.getStringExtra("key").toString()
                text = p1.getStringExtra("message").toString()

                val firebase = Firebase.database
                val reference = firebase.getReference("campaign")
                val campaignListener = object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val campaign = snapshot.getValue<Campaign>()
                        if (campaign != null) {
                            MainActivity.showNotification(p0.applicationContext, "You are currently in the nearest campaign - ${campaign.lat}, ${campaign.lon}" )
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        println("reminder:onCancelled: ${error.details}")
                    }
                }
                val child = reference.child(key)
                child.addValueEventListener(campaignListener)

                val triggeringGeofence = geofencingEvent.triggeringGeofences
                triggeringGeofence?.let { MainActivity.removeGeofenfences(p0, it) }
            }
        }
    }
}