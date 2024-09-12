package com.example.tutormatch.util

import android.widget.TextView
import androidx.databinding.BindingAdapter
import java.text.SimpleDateFormat
import java.util.*

@BindingAdapter("timestamp")
fun setTimestamp(textView: TextView, timestamp: Long) {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val date = Date(timestamp)
    textView.text = sdf.format(date)
}
@BindingAdapter("participants")
fun bindParticipants(textView: TextView, participants: List<String>?) {
    participants?.let {
        textView.text = it.joinToString(", ")
    }
}