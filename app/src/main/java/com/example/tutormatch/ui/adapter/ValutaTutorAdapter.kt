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
    private var tutors: List<Utente>,
    private val onRateTutor: (Utente, Int) -> Unit
) : RecyclerView.Adapter<ValutaTutorAdapter.TutorViewHolder>() {

    inner class TutorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tutorName: TextView = itemView.findViewById(R.id.tutor_name)
        val ratingSpinner: Spinner = itemView.findViewById(R.id.spinner_rating)
        val submitButton: Button = itemView.findViewById(R.id.button_submit_rating)

        fun bind(tutor: Utente) {
            tutorName.text = "${tutor.nome} ${tutor.cognome}"

            submitButton.setOnClickListener {
                val rating = ratingSpinner.selectedItem.toString().toIntOrNull()
                rating?.let {
                    onRateTutor(tutor, it)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TutorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tutor, parent, false)
        return TutorViewHolder(view)
    }

    override fun onBindViewHolder(holder: TutorViewHolder, position: Int) {
        val tutor = tutors[position]
        holder.bind(tutor)
    }

    override fun getItemCount(): Int = tutors.size

    // Metodo per aggiornare i dati della lista di tutor
    fun updateData(newTutors: List<Utente>) {
        val diffCallback = TutorDiffCallback(tutors, newTutors)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        tutors = newTutors
        diffResult.dispatchUpdatesTo(this)
    }

    // DiffUtil per ottimizzare gli aggiornamenti
    class TutorDiffCallback(
        private val oldList: List<Utente>,
        private val newList: List<Utente>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].userId == newList[newItemPosition].userId
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}
