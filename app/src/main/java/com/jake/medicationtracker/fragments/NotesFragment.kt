package com.jake.medicationtracker.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.jake.medicationtracker.R
import com.jake.medicationtracker.models.NotesModel

class NotesFragment : Fragment(R.layout.notes_fragment) {
    private lateinit var etNotes: EditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Load notepad data from database and retrieve reference to the edit text view
        NotesModel(context!!).use {
            val view = inflater.inflate(R.layout.notes_fragment, container, false)
            etNotes = view.findViewById(R.id.etNotes)
            etNotes.setText(it.loadNotes())

            return view
        }
    }

    override fun onPause() {
        super.onPause()

        // Save current state of the notepad to database
        NotesModel(context!!).use {
            it.saveNotes(etNotes.text.toString())
        }
    }
}