package com.jake.medicationtracker.models

import android.content.ContentValues
import android.content.Context
import com.jake.medicationtracker.pojo.Appointment
import java.io.Closeable

class AppointmentsModel(context: Context) : Closeable {
    // Connection to database
    private val conn = DBConnection(context)

    fun addAppointment(date: String, name: String, address: String): Int {
        // Construct a list of key-pairs
        val values = ContentValues()
        values.put("aDateTime", date)
        values.put("aDrName", name)
        values.put("aAddress", address)

        // Insert them into the appointments table
        conn.addEntry("appointments", values)

        // Retrieve ID from entry just added
        val cursor = conn.getAllEntriesCursor("appointments")
        cursor.moveToLast()
        return cursor.getInt(cursor.getColumnIndex("aID"))
    }

    fun updateAppointment(id: Int, date: String, name: String, address: String) {
        val values = ContentValues()
        values.put("aDateTime", date)
        values.put("aDrName", name)
        values.put("aAddress", address)

        conn.updateEntry("appointments", "aID=?", id, values)
    }

    fun deleteAppointment(id: Int) {
        // Delete appointment from database by ID
        conn.deleteEntry("appointments", "aID=?", id)
    }

    fun getAppointments(): ArrayList<Appointment> {
        // Create list and execute read query
        val data = ArrayList<Appointment>()
        val cursor = conn.getAllEntriesCursor("appointments")

        // Add all rows returned into ArrayList
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast) {
                val id = cursor.getInt(cursor.getColumnIndex("aID"))
                val date = cursor.getString(cursor.getColumnIndex("aDateTime"))
                val name = cursor.getString(cursor.getColumnIndex("aDrName"))
                val address = cursor.getString(cursor.getColumnIndex("aAddress"))

                data.add(Appointment(id, date, name, address))
                cursor.moveToNext()
            }
        }

        return data
    }

    override fun close() {
        // Close database connection
        conn.close()
    }
}