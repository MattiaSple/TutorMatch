package com.example.tutormatch.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tutormatch.data.model.Calendario
import com.example.tutormatch.databinding.ItemOrarioBinding


class CalendarioAdapter(
    private val onDeleteClick: (Calendario) -> Unit
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

    inner class CalendarioViewHolder(private val _binding: ItemOrarioBinding) : RecyclerView.ViewHolder(_binding.root) {

        fun bind(calendario: Calendario) {
            _binding.calendario = calendario
            _binding.executePendingBindings()

            _binding.btnDelete.setOnClickListener {
                onDeleteClick(calendario)
            }
        }
    }
}