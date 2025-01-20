package com.example.tutormatch.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tutormatch.data.model.Calendario
import com.example.tutormatch.databinding.ItemOrarioBinding

class CalendarioAdapter(
    private val onDeleteClick: (Calendario) -> Unit // Callback per il click sul pulsante elimina
) : RecyclerView.Adapter<CalendarioAdapter.CalendarioViewHolder>() {

    private var calendari = listOf<Calendario>() // Lista di oggetti `Calendario`

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarioViewHolder {
        // Crea un ViewHolder con il layout inflazionato
        val binding = ItemOrarioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CalendarioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CalendarioViewHolder, position: Int) {
        // Associa i dati al ViewHolder
        holder.bind(calendari[position])
    }

    override fun getItemCount(): Int = calendari.size // Restituisce il numero di elementi nella lista

    fun setCalendari(newCalendari: List<Calendario>) {
        // Aggiorna la lista e notifica i cambiamenti
        calendari = newCalendari
        notifyDataSetChanged()
    }

    inner class CalendarioViewHolder(private val _binding: ItemOrarioBinding) : RecyclerView.ViewHolder(_binding.root) {

        fun bind(calendario: Calendario) {
            // Collega i dati al layout
            _binding.calendario = calendario
            _binding.executePendingBindings()

            // Gestisce il click sul pulsante elimina
            _binding.btnDelete.setOnClickListener {
                onDeleteClick(calendario)
            }
        }
    }
}
