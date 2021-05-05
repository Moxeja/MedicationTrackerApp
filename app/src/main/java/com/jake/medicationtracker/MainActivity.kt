package com.jake.medicationtracker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jake.medicationtracker.fragments.AppointmentsFragment
import com.jake.medicationtracker.fragments.MedicationsFragment
import com.jake.medicationtracker.fragments.NotesFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Create notification channel
        createNotificationChannel()

        // Setup toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.materialToolbar)
        setSupportActionBar(toolbar)

        // Initialise three main fragments
        val medicationsFragment = MedicationsFragment()
        val appointmentsFragment = AppointmentsFragment()
        val notesFragment = NotesFragment()
        setNewFragment(medicationsFragment)

        // Switch to different fragment depending on which menu item is clicked
        findViewById<BottomNavigationView>(R.id.bottomNavigationView).setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.miMedications -> {
                    setNewFragment(medicationsFragment)
                    toolbar.title = "Medications"
                }

                R.id.miAppointments -> {
                    setNewFragment(appointmentsFragment)
                    toolbar.title = "Appointments"
                }

                R.id.miNotes -> {
                    setNewFragment(notesFragment)
                    toolbar.title = "Notes"
                }
            }
            true
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "medicationTrackerChannel"
            val descriptionText = "Channel for medication tracker notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("medicationTracker", name, importance).apply {
                description = descriptionText
            }

            // Register the channel with the system
            val notificationManager: NotificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Switch current fragment inside frame layout
    private fun setNewFragment(newFragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, newFragment)
            commit()
        }
}