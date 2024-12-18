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
    private var prenotazioniList: List<Prenotazione>,
    private val ruolo: Boolean,
    private val onDeleteClick: (Prenotazione) -> Unit,
    private val onChatClick: (Prenotazione) -> Unit
) : RecyclerView.Adapter<PrenotazioneAdapter.PrenotazioneViewHolder>() {

    inner class PrenotazioneViewHolder(private val binding: ItemPrenotazioneBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(prenotazione: Prenotazione) {
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
                    val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
                    val formattedDate = dateFormat.format(calendario.data)
                    binding.tvData.text = "Data: ${formattedDate}"
                    binding.tvOrarioLezione.text = "Orario: ${calendario.oraInizio} - ${calendario.oraFine}"
                    binding.tvModalita.text = when {
                        annuncio.mod_on && annuncio.mod_pres -> "Modalità: Sia online che in presenza"
                        annuncio.mod_on -> "Modalità: Online"
                        else -> "Modalità: Presenza"
                    }
                },
                onFailure = {
                }
            )

            binding.btnDelete.setOnClickListener {
                onDeleteClick(prenotazione)
            }

            binding.btnChat.setOnClickListener {
                onChatClick(prenotazione)
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
