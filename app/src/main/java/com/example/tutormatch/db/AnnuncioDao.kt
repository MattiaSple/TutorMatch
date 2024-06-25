package com.example.tutormatch.db

import androidx.room.*

@Dao
interface AnnuncioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserisci(annuncio: Annuncio)

    @Update
    suspend fun aggiorna(annuncio: Annuncio)

    @Delete
    suspend fun elimina(annuncio: Annuncio)

    @Query("SELECT * FROM annuncio WHERE id = :id")
    suspend fun getAnnuncioById(id: Int): Annuncio?

    @Query("SELECT * FROM annuncio")
    suspend fun getAllAnnunci(): List<Annuncio>

}
