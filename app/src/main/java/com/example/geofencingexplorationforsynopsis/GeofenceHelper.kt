package com.example.geofencingexplorationforsynopsis

import com.google.android.gms.location.Geofence
import com.google.android.gms.maps.model.LatLng
import java.util.UUID


class GeofenceHelper {
    fun createGeofence(latLng: LatLng, radius: Float, transitionTypes: Int): Geofence {
        return Geofence.Builder()
            .setRequestId(generateRequestId())
            .setCircularRegion(latLng.latitude, latLng.longitude, radius)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(transitionTypes)
            .build()
    }

    private fun generateRequestId(): String {
        return UUID.randomUUID().toString()
    }
}