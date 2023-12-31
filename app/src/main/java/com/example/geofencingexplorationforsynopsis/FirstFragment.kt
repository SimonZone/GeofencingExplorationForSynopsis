package com.example.geofencingexplorationforsynopsis

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.geofencingexplorationforsynopsis.databinding.FragmentFirstBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng


class FirstFragment : Fragment() {
    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val geofenceList: MutableMap<String, Geofence> = mutableMapOf()
    private val geofenceViewModel: GeofenceViewModel by activityViewModels()
    private val TAG: String = "FirstFragment"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)

        geofencingClient = LocationServices.getGeofencingClient(requireActivity())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        return binding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addGeofence("Hospital", location = Location("").apply {
            latitude = 55.6379
            longitude = 12.0888
        },  radiusInMeters = 100f)
        addGeofence("Hjem Korallen", location = Location("").apply {
            latitude = 55.6516
            longitude = 12.1423
        },  radiusInMeters = 100f)
        addGeofence("Zealand", location = Location("").apply {
            latitude = 55.6302
            longitude = 12.0784
        },  radiusInMeters = 100f)
        addGeofence("Intersection", location = Location("").apply {
            latitude = 55.6308160996
            longitude = 12.0891894302
        },  radiusInMeters = 50f)
        val locationPermissionRequest =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions())
            { permissions ->
                when {
                    permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)
                    || permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)
                    || permissions.getOrDefault(Manifest.permission.ACCESS_BACKGROUND_LOCATION, false) -> {
                        // Precise or Approximate location access granted.
                        Log.i(TAG, "Location access granted")
                        geofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                            .addOnSuccessListener {
                                Log.i(TAG, "added geofences")
                                getCurrentLocation()
                            }
                            .addOnFailureListener { e ->
                                // Geofences failed to be added
                                Log.e(TAG, "Error adding geofences", e)
                            }
                    }
                    else -> {
                        // No location access granted.
                        Log.i(TAG, "No location access granted")
                        showInContextUI()
                    }
                }
            }
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS
            )
        )
        binding.buttonMap.setOnClickListener {
            val action = FirstFragmentDirections.actionFirstFragmentToMapsFragment()
            findNavController().navigate(action)
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation(){
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener(requireActivity()) { location ->
                location?.let {
                    val latitude = it.latitude
                    val longitude = it.longitude
                    val accuracy = it.accuracy
                    geofenceViewModel.latLng.value = LatLng(latitude, longitude)
                    geofenceViewModel.geofenceList.value = geofenceList
                    if (geofenceViewModel.latLng.value == null) {
                        Log.d(
                            TAG,
                            "Current Location is: Latitude: $latitude, Longitude: $longitude, Accuracy: $accuracy meters"
                        )
                    }
                    binding.buttonMap.isEnabled = true
                } ?: run {
                    Log.e(TAG, "Location is null")
                }
            }
            .addOnFailureListener(requireActivity()) { e ->
                Log.e(TAG, "Error getting location", e)
            }
    }
    private fun getGeofencingRequest(): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            addGeofences(geofenceList.values.toList())
        }.build()
    }
    private fun getGeofencePendingIntent(): PendingIntent {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        return PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_MUTABLE)
    }
    private fun addGeofence(
        key: String,
        location: Location,
        radiusInMeters: Float = 5.0f,
        expirationTimeInMillis: Long = Geofence.NEVER_EXPIRE,
    ) {
        Log.v(TAG, "key: $key, Location: $location, Radius: $radiusInMeters, expiration: $expirationTimeInMillis")
        geofenceList[key] = createGeofence(key, location, radiusInMeters, expirationTimeInMillis)
    }
    private fun createGeofence(
        key: String,
        location: Location,
        radiusInMeters: Float,
        expirationTimeInMillis: Long,
    ): Geofence {
        return Geofence.Builder()
            .setRequestId(key)
            .setCircularRegion(location.latitude, location.longitude, radiusInMeters)
            .setExpirationDuration(expirationTimeInMillis)
            .setTransitionTypes(GEOFENCE_TRANSITION_ENTER or GEOFENCE_TRANSITION_EXIT)
            .build()
    }

    private fun showInContextUI() {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setTitle("Location Permission Required")
        dialogBuilder.setMessage("To provide you with location-based services, we need access to your device's precise location. " +
                "Please grant the location permission in the apps settings to enable this feature.")

        dialogBuilder.setNeutralButton("Ok") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = dialogBuilder.create()
        dialog.show()
    }
}