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
import com.jake.medicationtracker.AppointmentsActivity
import com.jake.medicationtracker.R
import com.jake.medicationtracker.adapters.AppointmentRecyclerAdapter
import com.jake.medicationtracker.models.AppointmentsModel

class AppointmentsFragment : Fragment(R.layout.appointments_fragment) {

    private lateinit var adapter: AppointmentRecyclerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.appointments_fragment, container, false)

        // Setup appointment adding FloatingActionButton
        view.findViewById<FloatingActionButton>(R.id.fabAddAppointment).setOnClickListener {
            // Create and start new activity intent
            val intent = Intent(context, AppointmentsActivity::class.java)
            startActivity(intent)
        }

        AppointmentsModel(context!!).use {
            // Setup RecyclerView adapter
            val rvView = view.findViewById<RecyclerView>(R.id.rvAppointmentsList)
            adapter = AppointmentRecyclerAdapter(context!!, it.getAppointments())
            rvView.adapter = adapter
            rvView.layoutManager = LinearLayoutManager(context!!)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        // Update RecyclerView changes
        AppointmentsModel(context!!).use {
            adapter.appointments = it.getAppointments()
            adapter.notifyDataSetChanged()
        }
    }
}