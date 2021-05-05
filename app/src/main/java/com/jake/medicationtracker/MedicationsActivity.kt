package com.jake.medicationtracker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.jake.medicationtracker.models.MedicationsModel
import com.jake.medicationtracker.notifications.MedicationReminderBroadcast
import java.text.SimpleDateFormat
import java.util.*

class MedicationsActivity : AppCompatActivity() {
    // UI elements of interest
    private lateinit var etName: EditText
    private lateinit var etDose: EditText
    private lateinit var etStock: EditText
    private lateinit var tpMedTime: TimePicker

    // Used to update an existing medication entry
    private var id = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medications)

        // Setup toolbar, code adapted from: https://stackoverflow.com/a/34997702
        val toolbar = findViewById<MaterialToolbar>(R.id.MedicationsToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Retrieve references to UI views
        etName = findViewById(R.id.etMedicationName)
        etDose = findViewById(R.id.etDose)
        etStock = findViewById(R.id.etStock)
        tpMedTime = findViewById(R.id.tpMedTime)

        // Check if in edit mode
        id = intent.getIntExtra("id", -1)
        if (id >= 0) {
            // Read information from intent
            val name = intent.getStringExtra("name")
            val time = intent.getStringExtra("time")
            val dose = intent.getIntExtra("dose", -1)
            val stock = intent.getIntExtra("stock", -1)

            // Update UI to reflect edit mode
            supportActionBar?.title = "Edit Medication"
            etName.setText(name)
            etDose.setText(dose.toString())
            etStock.setText(stock.toString())
            findViewById<Button>(R.id.btnAddMedication).text = getString(R.string.save_changes)
            setupTimePicker(time!!)

            // Setup edit button callback
            findViewById<Button>(R.id.btnAddMedication).setOnClickListener {
                // Make sure no text boxes are empty
                if (!checkInvalidInputs()) {
                    // Update database entry, reset notifications, and close activity
                    updateMedicationInDatabase()
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
            // Setup add medication button
            findViewById<Button>(R.id.btnAddMedication).setOnClickListener {
                // Make sure no text boxes are empty
                if (!checkInvalidInputs()) {
                    // Add medication to database, setup notifications, and close activity
                    id = addMedicationToDatabase()
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
        return (etName.text.isNullOrBlank() ||
                etDose.text.isNullOrBlank() || etStock.text.isNullOrBlank())
    }

    // Code adapted from: https://www.youtube.com/watch?v=nl-dheVpt8o
    private fun setNotification(requestCode: Int) {
        // Get time
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, tpMedTime.hour)
            set(Calendar.MINUTE, tpMedTime.minute)
            set(Calendar.SECOND, 0)
        }

        // Create intent
        val intent = Intent(this, MedicationReminderBroadcast::class.java)
        intent.putExtra("id", id)
        intent.putExtra("name", etName.text.toString())
        intent.putExtra("time", getTimeString())
        intent.putExtra("dose", Integer.parseInt(etDose.text.toString()))
        intent.putExtra("stock", Integer.parseInt(etStock.text.toString()))

        val pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent,
                PendingIntent.FLAG_CANCEL_CURRENT)

        // Schedule alarms
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis, pendingIntent)
    }

    // Code adapted from: https://stackoverflow.com/a/11682008
    private fun deleteNotification(requestCode: Int) {
        // Create intent and pending intent
        val intent = Intent(this, MedicationReminderBroadcast::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT)

        // Create alarm manager and cancel intent and alarm
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        pendingIntent.cancel()
        alarmManager.cancel(pendingIntent)
    }

    private fun setupTimePicker(time: String) {
        // Setup time picker
        val hoursMinutes = time.split(":")
        val hour = Integer.parseInt(hoursMinutes[0])
        val minute = Integer.parseInt(hoursMinutes[1])
        tpMedTime.hour = hour
        tpMedTime.minute = minute
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
                        .setMessage("Do you really want to delete this medication?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes) { _: DialogInterface, _: Int ->
                            deleteMedication()
                            deleteNotification(id)
                            finish()
                        }
                        .setNegativeButton(android.R.string.no, null)
                        .show()
            }
        }

        return true
    }

    private fun deleteMedication() {
        // Delete medication from database
        MedicationsModel(this).use {
            it.deleteMedication(id)
        }
    }

    private fun updateMedicationInDatabase() {
        // Write information to database
        MedicationsModel(this).use {
            it.updateMedication(id, etName.text.toString(), getTimeString(),
                    Integer.parseInt(etDose.text.toString()),
                    Integer.parseInt(etStock.text.toString()))
        }
    }

    private fun addMedicationToDatabase(): Int {
        // Write information to database
        MedicationsModel(this).use {
            return it.addMedication(etName.text.toString(), getTimeString(),
                    Integer.parseInt(etDose.text.toString()),
                    Integer.parseInt(etStock.text.toString()))
        }
    }

    private fun getTimeString(): String {
        // Get time information
        val hour = tpMedTime.hour
        val minute = tpMedTime.minute

        val calendar = GregorianCalendar()
        calendar.clear()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)

        // Format date and time string
        val formatter = SimpleDateFormat("HH:mm")
        return formatter.format(calendar.time)
    }
}