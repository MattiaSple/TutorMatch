package com.example.tutormatch.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tutormatch.data.model.Calendario
import com.example.tutormatch.databinding.ItemSelezionaFasciaBinding

class SelezioneFasceOrarieAdapter(
    private val onItemClick: (Calendario) -> Unit     // Callback per gestire la selezione della fascia oraria
) : RecyclerView.Adapter<SelezioneFasceOrarieAdapter.CalendarioViewHolder>() {

    private var fasceOrarie = listOf<Calendario>()    // Lista di fasce orarie da mostrare

    // Metodo per aggiornare la lista di fasce orarie
    fun setFasceOrarie(newFasceOrarie: List<Calendario>) {
        fasceOrarie = newFasceOrarie
        notifyDataSetChanged() // Notifica l'Adapter che i dati sono cambiati
    }

    // ViewHolder rappresenta una singola riga nel RecyclerView
    inner class CalendarioViewHolder(private val _binding: ItemSelezionaFasciaBinding) : RecyclerView.ViewHolder(_binding.root) {

        // Metodo per associare i dati della fascia oraria alla vista
        fun bind(fasciaOraria: Calendario) {
            // Imposta i dati della fascia oraria nel layout
            _binding.orarioInizio.text = fasciaOraria.oraInizio
            _binding.orarioFine.text = fasciaOraria.oraFine
        }
    }

    // Metodo chiamato quando il ViewHolder viene creato (il layout item)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarioViewHolder {
        val binding = ItemSelezionaFasciaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CalendarioViewHolder(binding)
    }

    // Metodo chiamato per associare i dati a una vista (ViewHolder) in una certa posizione
    override fun onBindViewHolder(holder: CalendarioViewHolder, position: Int) {
        holder.bind(fasceOrarie[position]) // Collega la fascia oraria alla vista
    }

    // Restituisce il numero totale di elementi da visualizzare
    override fun getItemCount(): Int = fasceOrarie.size
}
