package com.example.tutormatch.db

import androidx.room.*

@Dao
interface PrenotazioneDao {

    @Insert
    fun insert(prenotazione: Prenotazione)

    @Delete
    fun delete(prenotazione: Prenotazione)

    @Query("SELECT materia FROM prenotazione WHERE email_studente = :email")
    fun getAllPrenotazioniByEmail(email: String): List<Prenotazione>

    @Transaction
    @Query("""
        SELECT
            p.materia,
            a.prezzo as annuncioPrezzo,
            c.data as calendarioData,
            c.email_prof as calendarioStatoPren
        FROM prenotazione p
        JOIN annuncio a ON p.id_annuncio = a.id
        JOIN calendario c ON p.id_calendario = c.id
        WHERE p.email_studente = :emailStudente AND p.id_calendario = :idCalendario
    """)
    fun getInfoPrenotazione(emailStudente: String, idCalendario: Int): Prenotazione
}