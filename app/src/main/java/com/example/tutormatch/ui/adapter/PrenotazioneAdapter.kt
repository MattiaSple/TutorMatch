package com.example.tutormatch.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tutormatch.data.model.Prenotazione
import com.example.tutormatch.databinding.ItemPrenotazioneBinding
import com.example.tutormatch.util.FirebaseUtil

class PrenotazioneAdapter(
    private var prenotazioniList: List<Prenotazione>,
    private val ruolo: Boolean,  // Flag per distinguere se chi visualizza è un tutor
    private val onDeleteClick: (Prenotazione) -> Unit  // Click listener per il pulsante elimina
) : RecyclerView.Adapter<PrenotazioneAdapter.PrenotazioneViewHolder>() {

    inner class PrenotazioneViewHolder(private val binding: ItemPrenotazioneBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(prenotazione: Prenotazione) {
            // Recupera in modo atomico i dettagli dell'annuncio e della fascia oraria tramite FirebaseUtil
            FirebaseUtil.getNomeCognomeUtenteAtomico(
                annuncioRef = prenotazione.annuncioRef!!,
                calendarioRef = prenotazione.fasciaCalendarioRef!!,
                studenteId = prenotazione.studenteRef,  // Passa l'userId dello studente
                isTutor = ruolo,  // Flag che indica se chi visualizza è un tutor
                onSuccess = { annuncio, calendario, nome, cognome ->
                    // Usa i dati recuperati
                    binding.tvMateria.text = "${annuncio.materia}"
                    binding.tvNomeCognome.text = if (ruolo) {
                        "Studente: $nome $cognome"
                    } else {
                        "Tutor: $nome $cognome"
                    }
                    binding.tvPrezzo.text = "Prezzo: ${annuncio.prezzo} €"
                    binding.tvOrarioLezione.text = "Orario: ${calendario.oraInizio} - ${calendario.oraFine}"
                    binding.tvModalita.text = "${annuncio.getModalita()}"
                },
                onFailure = { exception ->
                    // Gestisci l'errore
                    Log.e("Adapter", "Errore nel caricamento dei dati: ${exception.message}")
                }
            )

            // Imposta il click listener per il pulsante di eliminazione
            binding.btnDelete.setOnClickListener {
                onDeleteClick(prenotazione)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrenotazioneViewHolder {
        val binding = ItemPrenotazioneBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PrenotazioneViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PrenotazioneViewHolder, position: Int) {
        holder.bind(prenotazioniList[position])
    }

    override fun getItemCount(): Int = prenotazioniList.size

    fun updatePrenotazioni(newPrenotazioniList: List<Prenotazione>) {
        prenotazioniList = newPrenotazioniList
        notifyDataSetChanged()
    }
}
