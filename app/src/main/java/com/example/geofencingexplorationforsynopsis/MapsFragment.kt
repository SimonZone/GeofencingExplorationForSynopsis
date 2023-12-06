package com.example.geofencingexplorationforsynopsis

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsFragment : Fragment() {
    private val geofenceViewModel: GeofenceViewModel by activityViewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }
    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { gMap ->
        googleMap = gMap
        googleMap.isMyLocationEnabled = true
        updateCameraToUserLocation()
        val geofenceList = geofenceViewModel.geofenceList.value

        googleMap.clear()
        geofenceList?.values?.forEach { geofence ->
            val markerOptions = MarkerOptions()
                .position(LatLng(geofence.latitude, geofence.longitude))
                .title(geofence.requestId)
            googleMap.addMarker(markerOptions)

            val circleOptions = CircleOptions()
                .center(LatLng(geofence.latitude, geofence.longitude))
                .radius(geofence.radius.toDouble())
                .strokeWidth(2f)
                .strokeColor(Color.argb(255, 255, 0, 0))
                .fillColor(Color.argb(70, 255, 0, 0))
            googleMap.addCircle(circleOptions)
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateCameraToUserLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val userLatLng = LatLng(location.latitude, location.longitude)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 16f))
            }
        }
    }
}