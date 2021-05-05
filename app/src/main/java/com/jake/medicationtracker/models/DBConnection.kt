package com.jake.medicationtracker.models

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import java.io.Closeable

class DBConnection(context: Context) : Closeable {

    // Create database connection
    private val db: SQLiteDatabase = context.openOrCreateDatabase("medications.sqlite", Context.MODE_PRIVATE, null)

    init {
        // Ensure table for medications is set up
        db.execSQL("CREATE TABLE IF NOT EXISTS medications (mID INTEGER PRIMARY KEY," +
                "mName TEXT NOT NULL, mTime TEXT NOT NULL, mDose INTEGER NOT NULL," +
                "mStock INTEGER NOT NULL)")

        // Ensure table for doctor appointments is set up
        db.execSQL("CREATE TABLE IF NOT EXISTS appointments (aID INTEGER PRIMARY KEY," +
                "aDateTime TEXT NOT NULL, aDrName TEXT NOT NULL, aAddress TEXT NOT NULL)")

        // Ensure table for notes is set up
        db.execSQL("CREATE TABLE IF NOT EXISTS notes (nID INTEGER PRIMARY KEY, nData TEXT)")
    }

    fun <T> updateEntry(table: String, whereClause: String, identifier: T, values: ContentValues) {
        // Check if row actually exists first
        val cursor = getSingleEntryCursor(table, whereClause, identifier)

        // If entry doesn't exist, add it as a new entry
        if (cursor.count < 1) {
            addEntry(table, values)
            return
        }

        // Update a row based on a where clause
        db.update(table, values, whereClause, arrayOf(identifier.toString()))
    }

    fun addEntry(table: String, values: ContentValues) {
        // Insert a row into specified table
        db.insert(table, null, values)
    }

    fun <T> deleteEntry(table: String, whereClause: String, identifier: T) {
        // Delete entry according to where clause and value
        db.delete(table, whereClause, arrayOf(identifier.toString()))
    }

    fun getAllEntriesCursor(table: String): Cursor {
        return db.rawQuery("SELECT * FROM $table", null)
    }

    private fun <T> getSingleEntryCursor(table: String, whereClause: String, identifier: T): Cursor {
        val where = whereClause.replace("?", identifier.toString())
        return db.rawQuery("SELECT * FROM $table WHERE $where", null)
    }

    override fun close() {
        // Close database
        db.close()
    }
}