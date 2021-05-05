package com.jake.medicationtracker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.jake.medicationtracker.models.AppointmentsModel
import com.jake.medicationtracker.notifications.AppointmentReminderBroadcast
import java.text.SimpleDateFormat
import java.util.*

class AppointmentsActivity : AppCompatActivity() {
    // UI elements of interest
    private lateinit var etName: EditText
    private lateinit var etPlace: EditText
    private lateinit var dpDate: DatePicker
    private lateinit var tpTime: TimePicker

    // Used to update an existing appointment entry
    private var id = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointments)

        // Setup toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.AppointmentToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Retrieve references to UI views
        etName = findViewById(R.id.etDrName)
        etPlace = findViewById(R.id.etLocation)
        dpDate = findViewById(R.id.dpDate)
        tpTime = findViewById(R.id.tpTime)

        // Check if in edit mode
        id = intent.getIntExtra("id", -1)
        if (id >= 0) {
            // Read information from intent
            val drName = intent.getStringExtra("drName")
            val dateTime = intent.getStringExtra("dateTime")?.split(" ")!!
            val surgery = intent.getStringExtra("surgery")

            // Update UI to reflect edit mode
            supportActionBar?.title = "Edit Appointment"
            etName.setText(drName)
            etPlace.setText(surgery)
            findViewById<Button>(R.id.btnAddAppointment).text = getString(R.string.save_changes)
            setupPickerDefaults(dateTime)

            // Setup edit button callback
            findViewById<Button>(R.id.btnAddAppointment).setOnClickListener {
                // Make sure no text boxes are empty
                if (!checkInvalidInputs()) {
                    // Update database entry, reset notifications, and close activity
                    updateAppointmentInDatabase()
                    deleteNotification(id)
                    setNotification(id)
                    finish()
                } else {
                    // Tell the user that they must fill all text boxes
                    Toast.makeText(this,
                            "Cannot leave text boxes empty!", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // Setup add appointment button
            findViewById<Button>(R.id.btnAddAppointment).setOnClickListener {
                // Make sure no text boxes are empty
                if (!checkInvalidInputs()) {
                    // Add appointment to database, setup notifications, and close activity
                    id = addAppointmentToDatabase()
                    setNotification(id)
                    finish()
                } else {
                    // Tell the user that they must fill all text boxes
                    Toast.makeText(this,
                            "Cannot leave text boxes empty!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkInvalidInputs(): Boolean {
        // Check to see if any input boxes are empty or null
        return (etName.text.isNullOrBlank() || etPlace.text.isNullOrBlank())
    }

    private fun setNotification(requestCode: Int) {
        // Create broadcast intent with extra data
        val intent = Intent(this, AppointmentReminderBroadcast::class.java)
        intent.putExtra("drName", etName.text.toString())

        // Create the pending intent
        val pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent,
                PendingIntent.FLAG_CANCEL_CURRENT)

        // Create the alarm which will create the notification
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                getDateTimeMillisMinusHour(), pendingIntent)
    }

    private fun deleteNotification(requestCode: Int) {
        // Create intent and pending intent
        val intent = Intent(this, AppointmentReminderBroadcast::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT)

        // Create alarm manager and cancel both intent and alarm
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        pendingIntent.cancel()
        alarmManager.cancel(pendingIntent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate menu and add delete button if necessary
        return if (id >= 0) {
            menuInflater.inflate(R.menu.edit_toolbar_menu, menu)
            true
        } else {
            super.onCreateOptionsMenu(menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // If the user clicked the back arrow
            android.R.id.home -> {
                finish()
            }

            // If the user clicked the delete icon
            R.id.miDeleteItem -> {
                // Ask user if they are sure they want to delete the entry
                AlertDialog.Builder(this)
                        .setTitle("Delete Entry?")
                        .setMessage("Do you really want to delete this appointment?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes) { _: DialogInterface, _: Int ->
                            deleteAppointment()
                            deleteNotification(id)
                            finish()
                        }
                        .setNegativeButton(android.R.string.no, null)
                        .show()
            }
        }

        return true
    }

    private fun deleteAppointment() {
        // Delete appointment from database
        AppointmentsModel(this).use {
            it.deleteAppointment(id)
        }
    }

    private fun setupPickerDefaults(dateTime: List<String>) {
        // Setup date picker
        val date = dateTime[0].split("-")
        val year = Integer.parseInt(date[0])
        val month = Integer.parseInt(date[1]) - 1 // Zero index
        val day = Integer.parseInt(date[2])
        dpDate.init(year, month, day, null)

        // Setup time picker
        val time = dateTime[1].split(":")
        val hour = Integer.parseInt(time[0])
        val minute = Integer.parseInt(time[1])
        tpTime.hour = hour
        tpTime.minute = minute
    }

    private fun getDateTimeMillisMinusHour(): Long {
        // Get date information
        val day = dpDate.dayOfMonth
        val month = dpDate.month
        val year = dpDate.year

        // Get time information
        val hour = tpTime.hour
        val minute = tpTime.minute

        // Construct date/time string
        val calendar = GregorianCalendar()
        calendar.clear()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, day)
        calendar.set(Calendar.HOUR_OF_DAY, hour - 1)
        calendar.set(Calendar.MINUTE, minute)

        return calendar.timeInMillis
    }

    private fun getDateTime(): String {
        // Get date information
        val day = dpDate.dayOfMonth
        val month = dpDate.month
        val year = dpDate.year

        // Get time information
        val hour = tpTime.hour
        val minute = tpTime.minute

        // Construct date/time string
        val calendar = GregorianCalendar()
        calendar.clear()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, day)
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)

        // Format date and time string
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm")
        return formatter.format(calendar.time)
    }

    private fun updateAppointmentInDatabase() {
        // Update appointment in database
        AppointmentsModel(this).use {
            it.updateAppointment(id, getDateTime(), etName.text.toString(), etPlace.text.toString())
        }
    }

    private fun addAppointmentToDatabase(): Int {
        // Write information to database
        AppointmentsModel(this).use {
            return it.addAppointment(getDateTime(), etName.text.toString(), etPlace.text.toString())
        }
    }
}