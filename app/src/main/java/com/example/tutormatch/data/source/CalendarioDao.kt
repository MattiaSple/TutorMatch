package com.example.tutormatch.data.source

import androidx.room.*
import com.example.tutormatch.data.model.Calendario

@Dao
interface CalendarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun inserisci(calendario: Calendario)

    @Update
    fun aggiorna(calendario: Calendario)

    @Delete
    fun elimina(calendario: Calendario)

    @Query("SELECT * FROM calendario WHERE id = :id")
    fun getCalendarioById(id: Int): Calendario?

    @Query("SELECT * FROM calendario")
    fun getAllCalendari(): List<Calendario>

    @Query("SELECT * FROM calendario WHERE email_prof = :emailProf")
    fun getCalendariByProf(emailProf: String): List<Calendario>
}
