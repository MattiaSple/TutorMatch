package com.example.tutormatch.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tutormatch.data.model.Annuncio
import com.example.tutormatch.databinding.ItemAnnuncioBinding

// Adapter per gestire una lista di oggetti Annuncio in un RecyclerView
class AnnuncioAdapter(
    // Funzione chiamata quando un annuncio viene eliminato
    private val onDeleteClick: (Annuncio) -> Unit
) : RecyclerView.Adapter<AnnuncioAdapter.AnnuncioViewHolder>() {

    // Lista di annunci gestita dall'adapter
    private var annunci = listOf<Annuncio>()

    // Metodo chiamato quando il ViewHolder deve essere creato
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnnuncioViewHolder {
        // Inflating il layout per ogni elemento della lista usando ViewBinding
        val binding = ItemAnnuncioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        // Restituisce un nuovo ViewHolder con il binding
        return AnnuncioViewHolder(binding)
    }

    // Metodo chiamato per associare i dati a un ViewHolder
    override fun onBindViewHolder(holder: AnnuncioViewHolder, position: Int) {
        // Chiama il metodo bind del ViewHolder per associare i dati dell'annuncio alla vista
        holder.bind(annunci[position])
    }

    // Restituisce il numero di elementi nella lista
    override fun getItemCount(): Int = annunci.size

    // Metodo per aggiornare la lista di annunci gestita dall'adapter
    fun setAnnunci(newAnnunci: List<Annuncio>) {
        annunci = newAnnunci
    }

    // ViewHolder interno per gestire le singole viste degli annunci
    inner class AnnuncioViewHolder(private val binding: ItemAnnuncioBinding) : RecyclerView.ViewHolder(binding.root) {

        // Metodo per associare un oggetto Annuncio ai componenti della vista
        fun bind(annuncio: Annuncio) {
            // Assegna l'annuncio corrente al binding
            binding.annuncio = annuncio
            // Aggiorna immediatamente il binding per riflettere eventuali modifiche
            binding.executePendingBindings()


            // Imposta il click listener per il bottone di eliminazione
            binding.btnDelete.setOnClickListener {
                // Chiama la funzione lambda onDeleteClick con l'annuncio corrente
                onDeleteClick(annuncio)
            }
        }
    }
}