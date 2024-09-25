package com.example.tutormatch.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tutormatch.data.model.Calendario
import com.example.tutormatch.databinding.ItemSelezionaFasciaBinding

class SelezioneFasceOrarieAdapter : RecyclerView.Adapter<SelezioneFasceOrarieAdapter.CalendarioViewHolder>() {

    private var fasceOrarie = listOf<Calendario>()  // Lista di fasce orarie da mostrare

    // Metodo per aggiornare la lista di fasce orarie
    fun setFasceOrarie(newFasceOrarie: List<Calendario>) {
        fasceOrarie = newFasceOrarie
        notifyDataSetChanged() // Notifica l'Adapter che i dati sono cambiati
    }

    // ViewHolder rappresenta una singola riga nel RecyclerView
    inner class CalendarioViewHolder(private val binding: ItemSelezionaFasciaBinding) : RecyclerView.ViewHolder(binding.root) {

        // Metodo per associare i dati della fascia oraria alla vista
        fun bind(fasciaOraria: Calendario) {
            // Imposta i dati della fascia oraria nel layout
            binding.orarioInizio.text = fasciaOraria.oraInizio
            binding.orarioFine.text = fasciaOraria.oraFine

            // Imposta lo stato della CheckBox in base alla selezione
            binding.checkBoxSeleziona.isChecked = false  // Di default la CheckBox non è selezionata
        }

        // Metodo per verificare se la `CheckBox` è selezionata
        fun isChecked(): Boolean {
            return binding.checkBoxSeleziona.isChecked
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
