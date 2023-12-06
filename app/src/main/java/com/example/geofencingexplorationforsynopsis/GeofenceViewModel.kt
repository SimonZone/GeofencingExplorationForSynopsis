package com.example.geofencingexplorationforsynopsis

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.Geofence
import com.google.android.gms.maps.model.LatLng

class GeofenceViewModel : ViewModel() {
    val latLng = MutableLiveData<LatLng>()
    val geofenceList = MutableLiveData<MutableMap<String, Geofence>>()
}