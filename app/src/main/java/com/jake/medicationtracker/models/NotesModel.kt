package com.jake.medicationtracker.models

import android.content.ContentValues
import android.content.Context
import java.io.Closeable

class NotesModel(context: Context) : Closeable {
    // Database connection
    private val conn = DBConnection(context)

    fun saveNotes(notes: String) {
        val values = ContentValues()
        values.put("nData", notes)

        // Save notes to database
        conn.updateEntry("notes", "nID=?", 1, values)
    }

    fun loadNotes(): String {
        var notes = ""
        val cursor = conn.getAllEntriesCursor("notes")

        // Retrieve notes from database and return them
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast) {
                notes = cursor.getString(cursor.getColumnIndex("nData"))
                cursor.moveToNext()
            }
        }

        return notes
    }

    override fun close() {
        // Close database connection
        conn.close()
    }
}