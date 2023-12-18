package com.example.geofencingexplorationforsynopsis

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    private val TAG: String = "GeofenceBroadcastReceiver"
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i(TAG,"Triggered geofence")
        val geofencingEvent = GeofencingEvent.fromIntent(intent!!)
        if (geofencingEvent?.hasError() == true) {
            val errorMessage = GeofenceStatusCodes
                .getStatusCodeString(geofencingEvent.errorCode)
            Log.e(TAG, errorMessage)
            return
        }
        if (geofencingEvent?.hasError() == false) {
            val geofenceTransition = geofencingEvent.geofenceTransition
            val geofence: Geofence? = geofencingEvent.triggeringGeofences?.firstOrNull()
            when (geofenceTransition) {
                Geofence.GEOFENCE_TRANSITION_ENTER -> showToast(context,"Entered geofence at ${geofence?.requestId}",TAG)
                Geofence.GEOFENCE_TRANSITION_EXIT -> showToast(context,"Exited geofence at ${geofence?.requestId}",TAG)
                else -> showToast(context,"Unknown geofence transition",TAG)
            }
        } else {
            Log.e(TAG, "GeofencingEvent: $geofencingEvent")
        }
    }
}
private fun showToast(context: Context?, message: String, TAG: String) {
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    Log.d(TAG, message)
}