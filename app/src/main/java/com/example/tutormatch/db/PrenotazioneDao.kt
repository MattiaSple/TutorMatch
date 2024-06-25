package com.example.tutormatch.db

import androidx.room.*

@Dao
interface PrenotazioneDao {
    @Insert
    suspend fun inserisci(prenotazione: Prenotazione)

    @Delete
    suspend fun delete(prenotazione: Prenotazione)


}