package com.example.tutormatch.db

import androidx.room.*

@Dao
interface CalendarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserisci(calendario: Calendario)

    @Update
    suspend fun aggiorna(calendario: Calendario)

    @Delete
    suspend fun elimina(calendario: Calendario)

    @Query("SELECT * FROM calendario WHERE id = :id")
    suspend fun getCalendarioById(id: Int): Calendario?

    @Query("SELECT * FROM calendario")
    suspend fun getAllCalendari(): List<Calendario>

    @Query("SELECT * FROM calendario WHERE email_prof = :emailProf")
    suspend fun getCalendariByProf(emailProf: String): List<Calendario>
}
