package com.jake.medicationtracker.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.jake.medicationtracker.R
import com.jake.medicationtracker.models.MedicationsModel
import java.security.SecureRandom
import java.util.*
import kotlin.math.max

class MedicationReminderBroadcast : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Retrieve extra information
        val id = intent.extras?.getInt("id")!!
        val medicationName = intent.extras?.getString("name")!!
        val time = intent.extras?.getString("time")!!
        val doseSize = intent.extras?.getInt("dose")!!
        val currentStock = intent.extras?.getInt("stock")!!

        // Calculate new stock amount
        var newStock = currentStock - doseSize
        newStock = max(newStock, 0)

        if (newStock > 0) {
            // Send medication remainder notification
            sendNotification(context, "Medication Due",
                    "$medicationName $doseSize tablet(s), $newStock tablet(s) remaining.")
        } else {
            // Send out of stock notification
            sendNotification(context, "Out of Medication",
                    "$medicationName has no doses left.")
        }

        // Update stock amount in database
        updateDatabaseEntry(context, id, medicationName, time, doseSize, newStock)

        // Setup new alarm for next day
        createNextAlarm(context, id, medicationName, time, doseSize, newStock)
    }

    private fun createNextAlarm(context: Context, id: Int, name: String,
                                time: String, dose: Int, stock: Int) {
        // Create new intent
        val intent2 = Intent(context, MedicationReminderBroadcast::class.java)
        intent2.putExtra("id", id)
        intent2.putExtra("name", name)
        intent2.putExtra("time", time)
        intent2.putExtra("dose", dose)
        intent2.putExtra("stock", stock)

        // Create new pending intent
        val pendingIntent = PendingIntent.getBroadcast(context, id, intent2,
                PendingIntent.FLAG_CANCEL_CURRENT)

        // Schedule alarms
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                getNextTimeMillis(time), pendingIntent)
    }

    private fun updateDatabaseEntry(context: Context, id: Int, name: String,
                                    time: String, dose: Int, stock: Int) {
        // Update medication entry in database
        MedicationsModel(context).use {
            it.updateMedication(id, name, time, dose, stock)
        }
    }

    private fun getNextTimeMillis(timeOfDay: String): Long {
        // Split time into integers
        val splitTime = timeOfDay.split(":")
        val hour = Integer.parseInt(splitTime[0])
        val minute = Integer.parseInt(splitTime[1])

        // Calculate time in milliseconds
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            add(Calendar.DAY_OF_MONTH, 1)
        }

        return calendar.timeInMillis
    }

    // Code adapted from: https://www.youtube.com/watch?v=nl-dheVpt8o
    private fun sendNotification(context: Context, title: String, description: String) {
        // Create the notification builder
        val builder = NotificationCompat.Builder(context, "medicationTracker")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(description)
                .setPriority(NotificationCompat.PRIORITY_HIGH)

        // Send the notification
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(SecureRandom().nextInt(), builder.build())
    }
}