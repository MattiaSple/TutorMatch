package com.example.tutormatch.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.tutormatch.R
import com.example.tutormatch.data.model.Utente

class ValutaTutorAdapter(
    private var tutors: List<Utente>, // Lista dei tutor da visualizzare
    private val onRateTutor: (Utente, Int) -> Unit // Callback per valutare un tutor
) : RecyclerView.Adapter<ValutaTutorAdapter.TutorViewHolder>() {

    // ViewHolder per rappresentare un singolo elemento della lista
    inner class TutorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tutorName: TextView = itemView.findViewById(R.id.tutor_name) // Nome del tutor
        val ratingSpinner: Spinner = itemView.findViewById(R.id.spinner_rating) // Spinner per scegliere la valutazione
        val submitButton: Button = itemView.findViewById(R.id.button_submit_rating) // Pulsante per inviare la valutazione

        fun bind(tutor: Utente) {
            // Imposta il nome del tutor nel TextView
            tutorName.text = "${tutor.nome} ${tutor.cognome}"

            // Gestisce il click sul pulsante per inviare la valutazione
            submitButton.setOnClickListener {
                val rating = ratingSpinner.selectedItem.toString().toIntOrNull() // Ottiene la valutazione selezionata
                rating?.let {
                    onRateTutor(tutor, it) // Chiama la callback con il tutor e il voto
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TutorViewHolder {
        // Crea il ViewHolder inflazionando il layout del singolo elemento
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tutor, parent, false)
        return TutorViewHolder(view)
    }

    override fun onBindViewHolder(holder: TutorViewHolder, position: Int) {
        // Collega i dati del tutor corrente al ViewHolder
        val tutor = tutors[position]
        holder.bind(tutor)
    }

    override fun getItemCount(): Int = tutors.size // Restituisce il numero di tutor nella lista

    // Metodo per aggiornare la lista dei tutor
    fun updateData(newTutors: List<Utente>) {
        val diffCallback = TutorDiffCallback(tutors, newTutors) // Calcola le differenze tra le liste
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        tutors = newTutors // Aggiorna la lista interna
        diffResult.dispatchUpdatesTo(this) // Applica gli aggiornamenti alla RecyclerView
    }

    // DiffUtil.Callback per ottimizzare gli aggiornamenti nella lista
    class TutorDiffCallback(
        private val oldList: List<Utente>, // Lista precedente
        private val newList: List<Utente> // Nuova lista
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size // Dimensione della lista precedente

        override fun getNewListSize(): Int = newList.size // Dimensione della nuova lista

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            // Confronta gli ID degli utenti per determinare se sono gli stessi
            return oldList[oldItemPosition].userId == newList[newItemPosition].userId
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            // Confronta il contenuto degli utenti per determinare se sono uguali
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}
