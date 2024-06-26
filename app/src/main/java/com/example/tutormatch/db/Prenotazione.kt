package com.example.tutormatch.db

import androidx.room.*
import java.util.Date

@Entity(
    tableName = "prenotazione",
    foreignKeys = [
        ForeignKey(
            entity = Annuncio::class,          // Tabella di riferimento
            parentColumns = ["id"],            // Colonna primaria della tabella di riferimento
            childColumns = ["id_annuncio"],    // Colonna nella tabella corrente che fa riferimento alla colonna primaria
            onDelete = ForeignKey.CASCADE      // Azione da eseguire quando la riga di riferimento viene eliminata
        ),
        ForeignKey(
            entity = Utente::class,            // Tabella di riferimento
            parentColumns = ["email"],         // Colonna primaria della tabella di riferimento
            childColumns = ["email_studente"], // Colonna nella tabella corrente che fa riferimento alla colonna primaria
            onDelete = ForeignKey.CASCADE      // Azione da eseguire quando la riga di riferimento viene eliminata
        )
    ]
)
data class Prenotazione(
    val email_studente: String,
    val materia: String,
    @PrimaryKey val id_calendario: Int,
    val id_annuncio: Int,
    val inizio_lez: Date?,
    val fine_lez: Date?
)