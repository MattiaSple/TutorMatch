package com.example.tutormatch.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "prenotazione")
data class Prenotazione(
    val email_studente: String,
    val materia: String,
    val id_calendario: Int,
    val id_annuncio: Int,
    val inizio_lez: Date,
    val fine_lez: Date)



