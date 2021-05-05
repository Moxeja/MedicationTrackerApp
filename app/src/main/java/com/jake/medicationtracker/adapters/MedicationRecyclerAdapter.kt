package com.jake.medicationtracker.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jake.medicationtracker.MedicationsActivity
import com.jake.medicationtracker.R
import com.jake.medicationtracker.pojo.Medication

// Code adapted from: https://www.youtube.com/watch?v=ZIU-SO77RpQ
class MedicationRecyclerAdapter(private val context: Context, var medications: ArrayList<Medication>) :
        RecyclerView.Adapter<MedicationRecyclerAdapter.MedicationsViewHolder>() {

    inner class MedicationsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicationRecyclerAdapter.MedicationsViewHolder {
        // Inflate and return inflated view holder
        val view = LayoutInflater.from(parent.context).inflate(R.layout.medications_list_view, parent, false)
        return MedicationsViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicationRecyclerAdapter.MedicationsViewHolder, position: Int) {
        // Update UI elements
        holder.itemView.findViewById<TextView>(R.id.tvMedicationName).text = medications[position].name
        holder.itemView.findViewById<TextView>(R.id.tvTime).text = medications[position].time
        holder.itemView.findViewById<TextView>(R.id.tvDoseSize).text = medications[position].doseSize.toString()
        holder.itemView.findViewById<TextView>(R.id.tvStock).text = medications[position].stockLeft.toString()

        holder.itemView.findViewById<RelativeLayout>(R.id.rlCardMeds).setOnClickListener {
            // Create new activity intent
            val intent = Intent(context, MedicationsActivity::class.java)

            // Add extra information
            intent.putExtra("id", medications[position].id)
            intent.putExtra("name", medications[position].name)
            intent.putExtra("time", medications[position].time)
            intent.putExtra("dose", medications[position].doseSize)
            intent.putExtra("stock", medications[position].stockLeft)

            // Start activity
            context.startActivity(intent)
        }
    }

    // Return internal list size
    override fun getItemCount() = medications.size
}