package com.jake.medicationtracker.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.jake.medicationtracker.R
import java.security.SecureRandom

class AppointmentReminderBroadcast : BroadcastReceiver() {

    // Code adapted from: https://www.youtube.com/watch?v=nl-dheVpt8o
    override fun onReceive(context: Context, intent: Intent) {
        // Retrieve extra information from intent
        val drName = intent.extras?.getString("drName")!!

        // Create and setup notification builder
        val builder = NotificationCompat.Builder(context, "medicationTracker")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Appointment Soon: $drName")
                .setContentText("You have an appointment within the next hour")
                .setPriority(NotificationCompat.PRIORITY_HIGH)

        // Show the notification
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(SecureRandom().nextInt(), builder.build())
    }
}