package com.example.tutormatch.data.model

import androidx.room.*


@Entity(
    tableName = "prenotazione",
    primaryKeys = ["email_studente", "id_calendario"],
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
        ),
        ForeignKey(
            entity = Calendario::class,         // Tabella di riferimento
            parentColumns = ["id"],             // Colonna primaria della tabella di riferimento
            childColumns = ["id_calendario"],   // Colonna nella tabella corrente che fa riferimento alla colonna primaria
            onDelete = ForeignKey.CASCADE       // Azione da eseguire quando la riga di riferimento viene eliminata
        )
    ]
)
data class Prenotazione(
    val email_studente: String,
    val materia: String,
    val id_calendario: Int,
    val id_annuncio: Int
)