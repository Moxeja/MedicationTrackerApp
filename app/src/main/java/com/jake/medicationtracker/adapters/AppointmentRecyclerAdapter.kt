package com.jake.medicationtracker.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jake.medicationtracker.AppointmentsActivity
import com.jake.medicationtracker.R
import com.jake.medicationtracker.pojo.Appointment

// Code adapted from: https://www.youtube.com/watch?v=ZIU-SO77RpQ
class AppointmentRecyclerAdapter(private val context: Context, var appointments: ArrayList<Appointment>) :
        RecyclerView.Adapter<AppointmentRecyclerAdapter.AppointmentsViewHolder>() {

    inner class AppointmentsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentsViewHolder {
        // Inflate and return a view holder
        val view = LayoutInflater.from(parent.context).inflate(R.layout.appointments_list_view, parent, false)
        return AppointmentsViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppointmentsViewHolder, position: Int) {
        // Update UI elements
        holder.itemView.findViewById<TextView>(R.id.tvDrName).text = appointments[position].drName
        holder.itemView.findViewById<TextView>(R.id.tvDate).text = appointments[position].dateTime
        holder.itemView.findViewById<TextView>(R.id.tvSurgery).text = appointments[position].address

        holder.itemView.findViewById<RelativeLayout>(R.id.rlCard).setOnClickListener {
            // Create new activity intent
            val intent = Intent(context, AppointmentsActivity::class.java)

            // Add extra information
            intent.putExtra("id", appointments[position].id)
            intent.putExtra("drName", appointments[position].drName)
            intent.putExtra("dateTime", appointments[position].dateTime)
            intent.putExtra("surgery", appointments[position].address)

            // Start activity
            context.startActivity(intent)
        }
    }

    // Return size of internal items list
    override fun getItemCount() = appointments.size
}