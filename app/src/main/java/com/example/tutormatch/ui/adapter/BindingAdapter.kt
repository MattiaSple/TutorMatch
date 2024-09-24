package com.example.tutormatch.util

import android.widget.TextView
import androidx.databinding.BindingAdapter
import java.text.SimpleDateFormat
import java.util.*

@BindingAdapter("timestamp")
fun setTimestamp(textView: TextView, timestamp: Long?) {
    // Verifica se il timestamp non è nullo
    if (timestamp != null && timestamp > 0) {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val date = Date(timestamp)
        textView.text = sdf.format(date)
    } else {
        // Se il timestamp non è valido o è nullo, mostra un testo di fallback
        textView.text = "Data non disponibile"
    }
}

@BindingAdapter("participants")
fun bindParticipants(textView: TextView, participants: List<String>?) {
    participants?.let {
        textView.text = it.joinToString(", ")
    }
}
