package com.example.tutormatch.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tutormatch.data.model.Annuncio
import com.example.tutormatch.databinding.ItemAnnuncioBinding

class AnnuncioAdapter : RecyclerView.Adapter<AnnuncioAdapter.AnnuncioViewHolder>() {

    private var annunci: List<Annuncio> = listOf()

    // Imposta la lista degli annunci e notifica il cambiamento
    fun setAnnunci(annunci: List<Annuncio>) {
        this.annunci = annunci
        notifyDataSetChanged()
    }

    // Crea un nuovo ViewHolder per ogni elemento
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnnuncioViewHolder {
        val binding = ItemAnnuncioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AnnuncioViewHolder(binding)
    }

    // Associa i dati dell'annuncio al ViewHolder
    override fun onBindViewHolder(holder: AnnuncioViewHolder, position: Int) {
        holder.bind(annunci[position])
    }

    // Restituisce il numero di elementi nella lista
    override fun getItemCount(): Int = annunci.size

    class AnnuncioViewHolder(private val binding: ItemAnnuncioBinding) : RecyclerView.ViewHolder(binding.root) {
        // Associa i dati dell'annuncio agli elementi della UI
        fun bind(annuncio: Annuncio) {
            binding.annuncio = annuncio
            binding.executePendingBindings()
        }
    }
}
