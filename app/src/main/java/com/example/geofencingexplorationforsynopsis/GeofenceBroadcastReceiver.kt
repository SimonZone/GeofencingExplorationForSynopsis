package com.example.geofencingexplorationforsynopsis

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    private val TAG: String = "GeofenceBroadcastReceiver"
    private var message: String = ""
    private val CHANNEL_ID: String = "geofenceApp"
    private lateinit var geofence: Geofence
    override fun onReceive(context: Context, intent: Intent?) {
        Log.i(TAG,"Triggered geofence")
        if (intent == null) {
            Log.e(TAG,"intent was null")
            return
        }
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent?.hasError() == true) {
            val errorMessage = GeofenceStatusCodes
                .getStatusCodeString(geofencingEvent.errorCode)
            Log.e(TAG, errorMessage)
            return
        }
        if (geofencingEvent?.hasError() == false) {
            val geofenceTransition = geofencingEvent.geofenceTransition

            if (geofencingEvent.triggeringGeofences?.first() == null) return
            geofence = geofencingEvent.triggeringGeofences?.first()!!
            when (geofenceTransition) {
                Geofence.GEOFENCE_TRANSITION_ENTER -> {
                    message = "Entered geofence at "
                    sendNotification(context)
                }
                Geofence.GEOFENCE_TRANSITION_EXIT -> {
                    message = "Exited geofence at "
                    sendNotification(context)
                }
            }
        } else {
            Log.e(TAG, "GeofencingEvent: $geofencingEvent")
        }
    }

    private fun createNotification(context: Context): Notification {
        Toast.makeText(context, message + geofence.requestId, Toast.LENGTH_LONG).show()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(androidx.core.R.drawable.notification_icon_background)
            .setContentTitle(message + geofence.requestId)
            .setContentText("With latitude: ${geofence.latitude} and longitude ${geofence.longitude}")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("With latitude: ${geofence.latitude} and longitude ${geofence.longitude}"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            // Set the intent that fires when the user taps the notification.
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        return builder.build()
    }
    private fun sendNotification(context: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.

        val name = "GeofenceAppNotificationChannel"
        val descriptionText = "This is a notification Channel"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system.
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        val notification = createNotification(context)
        notificationManager.notify(0, notification)
    }
}

