package com.jake.medicationtracker

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.jake.medicationtracker.models.AppointmentsModel
import com.jake.medicationtracker.notifications.AppointmentReminderBroadcast
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
class AppointmentsActivity : AppCompatActivity() {
    // UI elements of interest
    private lateinit var etName: EditText
    private lateinit var etPlace: EditText
    private lateinit var etDate: EditText
    private lateinit var etTime: EditText

    // Used to update an existing appointment entry
    private var id = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointments)

        // Setup toolbar, code adapted from: https://stackoverflow.com/a/34997702
        val toolbar = findViewById<MaterialToolbar>(R.id.AppointmentToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Retrieve references to UI views
        etName = findViewById(R.id.etDrName)
        etPlace = findViewById(R.id.etLocation)
        etDate = findViewById(R.id.etAppointmentDate)
        etTime = findViewById(R.id.etAppointmentTime)

        // Make date and time readouts readonly
        etTime.inputType = InputType.TYPE_NULL
        etDate.inputType = InputType.TYPE_NULL

        // Setup date and time picker dialogs
        setupDateTimePickers()

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
            etTime.setText(dateTime[1])
            etDate.setText(dateTime[0])
            findViewById<Button>(R.id.btnAddAppointment).text = getString(R.string.save_changes)

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

    private fun setupDateTimePickers() {
        // Setup date picker event handler
        val dateSet = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            // Get current calendar and set retrieved date
            val calendar = Calendar.getInstance()
            val formatter = SimpleDateFormat("yyyy-MM-dd")
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)

            // Set text box to selected date
            etDate.setText(formatter.format(calendar.time))
        }

        // Setup time picker event handler
        val timeSet = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            // Get current calendar and set retrieved time
            val calendar = Calendar.getInstance()
            val formatter = SimpleDateFormat("HH:mm")
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)

            // Set text box to selected time
            etTime.setText(formatter.format(calendar.time))
        }

        findViewById<Button>(R.id.btnAppointmentSelectTime).setOnClickListener {
            val calendar = Calendar.getInstance()

            // Check if a time has already been selected, set picker default to it
            if (!etTime.text.isNullOrBlank()) {
                val timeSplit = etTime.text.split(":")
                calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeSplit[0]))
                calendar.set(Calendar.MINUTE, Integer.parseInt(timeSplit[1]))
            }

            // Show the time picker dialog prompt
            TimePickerDialog(this, timeSet,
                calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }

        findViewById<Button>(R.id.btnAppointmentSelectDate).setOnClickListener {
            val calendar = Calendar.getInstance()

            // Check if a date has already been selected, set picker default to it
            if (!etDate.text.isNullOrBlank()) {
                val dateSplit = etDate.text.split("-")
                calendar.set(Calendar.YEAR, Integer.parseInt(dateSplit[0]))
                calendar.set(Calendar.MONTH, Integer.parseInt(dateSplit[1]) - 1)
                calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateSplit[2]))
            }

            // Show the date picker dialog prompt
            DatePickerDialog(this, dateSet, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun checkInvalidInputs(): Boolean {
        // Check to see if any input boxes are empty or null
        return (etName.text.isNullOrBlank() || etPlace.text.isNullOrBlank()
                || etTime.text.isNullOrBlank() || etDate.text.isNullOrBlank())
    }

    // Code adapted from: https://www.youtube.com/watch?v=nl-dheVpt8o
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

    // Code adapted from: https://stackoverflow.com/a/11682008
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

    // Code adapted from: https://stackoverflow.com/a/35649007
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate menu and add delete button if necessary
        return if (id >= 0) {
            menuInflater.inflate(R.menu.edit_toolbar_menu, menu)
            true
        } else {
            super.onCreateOptionsMenu(menu)
        }
    }

    // Code adapted from: https://stackoverflow.com/a/34997702
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

    private fun getDateTimeMillisMinusHour(): Long {
        // Get date information
        val dateSplit = etDate.text.split("-")
        val day = Integer.parseInt(dateSplit[2])
        val month = Integer.parseInt(dateSplit[1])
        val year = Integer.parseInt(dateSplit[0])

        // Get time information
        val timeSplit = etTime.text.split(":")
        val hour = Integer.parseInt(timeSplit[0])
        val minute = Integer.parseInt(timeSplit[1])

        // Construct date/time string
        val calendar = GregorianCalendar()
        calendar.clear()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1)
        calendar.set(Calendar.DAY_OF_MONTH, day)
        calendar.set(Calendar.HOUR_OF_DAY, hour - 1)
        calendar.set(Calendar.MINUTE, minute)

        return calendar.timeInMillis
    }

    private fun getDateTime(): String {
        // Get date information
        val dateSplit = etDate.text.split("-")
        val day = Integer.parseInt(dateSplit[2])
        val month = Integer.parseInt(dateSplit[1])
        val year = Integer.parseInt(dateSplit[0])

        // Get time information
        val timeSplit = etTime.text.split(":")
        val hour = Integer.parseInt(timeSplit[0])
        val minute = Integer.parseInt(timeSplit[1])

        // Construct date/time string
        val calendar = GregorianCalendar()
        calendar.clear()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1)
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