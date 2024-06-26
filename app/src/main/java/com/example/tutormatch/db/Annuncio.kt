package com.example.tutormatch.db

import androidx.room.*

@Entity(
    tableName = "annuncio",
    foreignKeys = [
        ForeignKey(
            entity = Utente::class,            // Tabella di riferimento
            parentColumns = ["email"],         // Colonna primaria della tabella di riferimento
            childColumns = ["email_prof"],     // Colonna nella tabella corrente che fa riferimento alla colonna primaria
            onDelete = ForeignKey.CASCADE      // Azione da eseguire quando la riga di riferimento viene eliminata
        )
    ]
)
data class Annuncio(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val email_prof: String,
    val materia: String,
    val prezzo: Int,
    val descrizione: String,
    val mod_ins: Boolean)


