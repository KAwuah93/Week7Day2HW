package com.example.mapsandlocation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            //val errorMessage = GeofenceErrorMessages.getErrorString(this,
            //    geofencingEvent.errorCode)
            Log.e("GEOFENCING", "Error = $geofencingEvent.errorCode")
            return
        }

        // Get the transition type.
        val geofenceTransition = geofencingEvent.geofenceTransition

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            val triggeringGeofences = geofencingEvent.triggeringGeofences

            // Get the transition details as a String.
//            val geofenceTransitionDetails = getGeofenceTransitionDetails(
//                this,
//                geofenceTransition,
//                triggeringGeofences
//            )

            // Send notification and log the transition details.
            //sendNotification(geofenceTransitionDetails)
            Toast.makeText(context, "Notification! Its working ?", Toast.LENGTH_LONG).show()
            Log.i("GEOFENCING", "welp")
        } else {
            // Log the error.
            Log.e("GEOFENCING","Error mayne")
        }
    }
}