package com.example.tutormatch.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tutormatch.data.model.Prenotazione
import com.example.tutormatch.databinding.ItemPrenotazioneBinding
import com.example.tutormatch.util.FirebaseUtil
import java.text.SimpleDateFormat
import java.util.Locale

class PrenotazioneAdapter(
    private var prenotazioniList: List<Prenotazione>, // Lista delle prenotazioni
    private val ruolo: Boolean, // Indica se l'utente è un tutor o uno studente
    private val onDeleteClick: (Prenotazione) -> Unit, // Callback per eliminare una prenotazione
    private val onChatClick: (Prenotazione) -> Unit // Callback per avviare una chat
) : RecyclerView.Adapter<PrenotazioneAdapter.PrenotazioneViewHolder>() {

    // ViewHolder per gestire il layout di ogni prenotazione
    inner class PrenotazioneViewHolder(private val binding: ItemPrenotazioneBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(prenotazione: Prenotazione) {
            // Recupera i dati della prenotazione tramite Firebase e li associa al layout
            FirebaseUtil.getNomeCognomeUtenteAtomico(
                annuncioRef = prenotazione.annuncioRef!!,
                calendarioRef = prenotazione.fasciaCalendarioRef!!,
                studenteId = prenotazione.studenteRef,
                isTutor = ruolo,
                onSuccess = { annuncio, calendario, nome, cognome ->
                    binding.tvMateria.text = "${annuncio.materia}"
                    binding.tvNomeCognome.text = if (ruolo) {
                        "Studente: $nome $cognome"
                    } else {
                        "Tutor: $nome $cognome"
                    }
                    binding.tvPrezzo.text = "Prezzo: ${annuncio.prezzo} €"

                    // Formatta e imposta la data e l'orario
                    val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
                    val formattedDate = dateFormat.format(calendario.data)
                    binding.tvData.text = "Data: ${formattedDate}"
                    binding.tvOrarioLezione.text = "Orario: ${calendario.oraInizio} - ${calendario.oraFine}"

                    // Imposta la modalità della lezione
                    binding.tvModalita.text = when {
                        annuncio.mod_on && annuncio.mod_pres -> "Modalità: Sia online che in presenza"
                        annuncio.mod_on -> "Modalità: Online"
                        else -> "Modalità: Presenza"
                    }
                },
                onFailure = {
                    // Gestione del fallimento (non implementata)
                }
            )

            // Gestisce il click sul pulsante elimina
            binding.btnDelete.setOnClickListener {
                onDeleteClick(prenotazione)
            }

            // Gestisce il click sul pulsante chat
            binding.btnChat.setOnClickListener {
                onChatClick(prenotazione)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrenotazioneViewHolder {
        // Crea il ViewHolder inflazionando il layout della prenotazione
        val binding = ItemPrenotazioneBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PrenotazioneViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PrenotazioneViewHolder, position: Int) {
        // Associa la prenotazione corrente al ViewHolder
        holder.bind(prenotazioniList[position])
    }

    override fun getItemCount(): Int = prenotazioniList.size // Restituisce il numero di prenotazioni

    fun updatePrenotazioni(newPrenotazioniList: List<Prenotazione>) {
        // Aggiorna la lista delle prenotazioni e notifica i cambiamenti
        prenotazioniList = newPrenotazioniList
        notifyDataSetChanged()
    }
}
