package com.jake.medicationtracker.models

import android.content.ContentValues
import android.content.Context
import com.jake.medicationtracker.pojo.Medication
import java.io.Closeable

class MedicationsModel(context: Context) : Closeable {
    // Database connection
    private val conn = DBConnection(context)

    fun addMedication(name: String, time: String, doseSize: Int, stock: Int): Int {
        // Construct a list of key-pairs
        val values = ContentValues()
        values.put("mName", name)
        values.put("mTime", time)
        values.put("mDose", doseSize)
        values.put("mStock", stock)

        // Insert them into the appointments table
        conn.addEntry("medications", values)

        // Retrieve ID from entry just added
        val cursor = conn.getAllEntriesCursor("medications")
        cursor.moveToLast()
        return cursor.getInt(cursor.getColumnIndex("mID"))
    }

    fun updateMedication(id: Int, name: String, time: String, doseSize: Int, stock: Int) {
        val values = ContentValues()
        values.put("mName", name)
        values.put("mTime", time)
        values.put("mDose", doseSize)
        values.put("mStock", stock)

        conn.updateEntry("medications", "mID=?", id, values)
    }

    fun deleteMedication(id: Int) {
        // Delete medication from database by ID
        conn.deleteEntry("medications", "mID=?", id)
    }

    fun getMedications(): ArrayList<Medication> {
        // Create list and execute read query
        val data = ArrayList<Medication>()
        val cursor = conn.getAllEntriesCursor("medications")

        // Add all rows returned into ArrayList
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast) {
                val id = cursor.getInt(cursor.getColumnIndex("mID"))
                val name = cursor.getString(cursor.getColumnIndex("mName"))
                val time = cursor.getString(cursor.getColumnIndex("mTime"))
                val dose = cursor.getInt(cursor.getColumnIndex("mDose"))
                val stock = cursor.getInt(cursor.getColumnIndex("mStock"))

                data.add(Medication(id, name, time, dose, stock))
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