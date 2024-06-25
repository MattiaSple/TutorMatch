package com.example.tutormatch.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "annuncio")
data class Annuncio(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val email_prof: String, //foreign key
    val materia: String,
    val prezzo: Int,
    val descrizione: String,
    val mod_ins: Boolean)


