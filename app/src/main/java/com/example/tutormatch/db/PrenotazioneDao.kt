package com.example.tutormatch.db

import androidx.room.*

@Dao
interface PrenotazioneDao {
    @Insert
    fun insert(prenotazione: Prenotazione)

    @Delete
    fun delete(prenotazione: Prenotazione)

    @Query("SELECT materia, inizio_lez FROM prenotazione WHERE email_studente = :email")
    fun getAllPrenotazioniByEmail(email: String): List<Prenotazione>



}