package com.example.tutormatch.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tutormatch.data.model.Calendario
import com.example.tutormatch.databinding.ItemSelezionaFasciaBinding

class SelezioneFasceOrarieAdapter(
    private val getFasceSelezionate: (List<Calendario>) -> Unit  // Callback per notificare la selezione
) : RecyclerView.Adapter<SelezioneFasceOrarieAdapter.CalendarioViewHolder>() {

    private var listaFasceOrarie = listOf<Calendario>()  // Lista di fasce orarie da mostrare
    private val selectedFasceOrarie = mutableListOf<Calendario>() // Fasce selezionate
    private val selezioneStati = mutableMapOf<Int, Boolean>() // Mappa per tracciare lo stato delle checkbox

    // Metodo per aggiornare la lista di fasce orarie
    fun setFasceOrarie(listaFasce: List<Calendario>) {
        listaFasceOrarie = listaFasce
        selectedFasceOrarie.clear() // Resetta le selezioni
        selezioneStati.clear() // Resetta gli stati delle selezioni
        notifyDataSetChanged() // Notifica l'Adapter che i dati sono cambiati
    }

    // ViewHolder rappresenta una singola riga nel RecyclerView
    inner class CalendarioViewHolder(private val binding: ItemSelezionaFasciaBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(fasciaOraria: Calendario, position: Int) {
            // Imposta i dati della fascia oraria nel layout
            binding.orarioLezione.text = "Lezione: " + fasciaOraria.oraInizio + " - " + fasciaOraria.oraFine

            // Imposta lo stato della CheckBox in base alla mappa delle selezioni
            binding.checkBoxSeleziona.setOnCheckedChangeListener(null) // Evita chiamate multiple al listener durante il riciclo

            // Controlla se lo stato della checkBox è già stato memorizzato nella mappa
            binding.checkBoxSeleziona.isChecked = selezioneStati[position] ?: false

            // Aggiungi o rimuovi la fascia selezionata dalla lista e aggiorna la mappa
            binding.checkBoxSeleziona.setOnCheckedChangeListener { _, isChecked ->
                selezioneStati[position] = isChecked // Aggiorna lo stato della checkBox nella mappa
                if (isChecked) {
                    selectedFasceOrarie.add(fasciaOraria)
                } else {
                    selectedFasceOrarie.remove(fasciaOraria)
                }
                getFasceSelezionate(selectedFasceOrarie) // Notifica la selezione
            }
        }
    }

    // Metodo chiamato quando il ViewHolder viene creato (il layout item)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarioViewHolder {
        val binding = ItemSelezionaFasciaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CalendarioViewHolder(binding)
    }

    // Metodo chiamato per associare i dati a una vista (ViewHolder) in una certa posizione
    override fun onBindViewHolder(holder: CalendarioViewHolder, position: Int) {
        holder.bind(listaFasceOrarie[position], position) // Collega la fascia oraria alla vista
    }

    // Restituisce il numero totale di elementi da visualizzare
    override fun getItemCount(): Int = listaFasceOrarie.size
}
