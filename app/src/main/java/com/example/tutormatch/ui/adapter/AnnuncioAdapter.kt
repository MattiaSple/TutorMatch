package com.example.tutormatch.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tutormatch.data.model.Annuncio
import com.example.tutormatch.databinding.ItemAnnuncioBinding

class AnnuncioAdapter(
    private val onDeleteClick: (Annuncio) -> Unit
) : RecyclerView.Adapter<AnnuncioAdapter.AnnuncioViewHolder>() {

    private var annunci = listOf<Annuncio>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnnuncioViewHolder {
        val binding = ItemAnnuncioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AnnuncioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AnnuncioViewHolder, position: Int) {
        holder.bind(annunci[position])
    }

    override fun getItemCount(): Int = annunci.size

    fun setAnnunci(newAnnunci: List<Annuncio>) {
        annunci = newAnnunci
        notifyDataSetChanged()
    }

    inner class AnnuncioViewHolder(private val binding: ItemAnnuncioBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(annuncio: Annuncio) {
            binding.annuncio = annuncio
            binding.executePendingBindings()

            // Imposta il click listener per il bottone elimina
            binding.btnDelete.setOnClickListener {
                onDeleteClick(annuncio)
            }
        }
    }
}
