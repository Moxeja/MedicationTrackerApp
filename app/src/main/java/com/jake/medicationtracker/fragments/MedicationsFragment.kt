package com.jake.medicationtracker.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jake.medicationtracker.MedicationsActivity
import com.jake.medicationtracker.R
import com.jake.medicationtracker.adapters.MedicationRecyclerAdapter
import com.jake.medicationtracker.models.MedicationsModel

// Code adapted from: https://www.youtube.com/watch?v=HtwDXRWjMcU
class MedicationsFragment : Fragment(R.layout.medications_fragment) {

    private lateinit var adapter: MedicationRecyclerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.medications_fragment, container, false)

        // Setup medication adding FloatingActionButton
        view.findViewById<FloatingActionButton>(R.id.fabAddMedication).setOnClickListener {
            // Create and start new activity intent
            val intent = Intent(context, MedicationsActivity::class.java)
            startActivity(intent)
        }

        MedicationsModel(context!!).use {
            // Setup RecyclerView adapter
            val rvView = view.findViewById<RecyclerView>(R.id.rvMedicationsList)
            adapter = MedicationRecyclerAdapter(context!!, it.getMedications())
            rvView.adapter = adapter
            rvView.layoutManager = LinearLayoutManager(context!!)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        // Update RecyclerView changes
        MedicationsModel(context!!).use {
            adapter.medications = it.getMedications()
            adapter.notifyDataSetChanged()
        }
    }
}