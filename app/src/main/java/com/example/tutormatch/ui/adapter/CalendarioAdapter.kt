package com.example.tutormatch.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tutormatch.data.model.Calendario
import com.example.tutormatch.databinding.ItemOrarioBinding
import java.text.SimpleDateFormat
import java.util.*

class CalendarioAdapter(
    private val onDeleteClick: (Calendario) -> Unit,
    private val onUpdateOrariInizio: (String?) -> Unit
) : RecyclerView.Adapter<CalendarioAdapter.CalendarioViewHolder>() {

    private var calendari = listOf<Calendario>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarioViewHolder {
        val binding = ItemOrarioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CalendarioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CalendarioViewHolder, position: Int) {
        holder.bind(calendari[position])
    }

    override fun getItemCount(): Int = calendari.size

    fun setCalendari(newCalendari: List<Calendario>) {
        calendari = newCalendari
        notifyDataSetChanged()
    }

    inner class CalendarioViewHolder(private val binding: ItemOrarioBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(calendario: Calendario) {
            binding.calendario = calendario
            binding.executePendingBindings()

            binding.btnDelete.setOnClickListener {
                onDeleteClick(calendario)
            }
        }
    }
}
