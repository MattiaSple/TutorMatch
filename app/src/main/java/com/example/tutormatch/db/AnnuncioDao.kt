package com.example.tutormatch.db

import androidx.room.*

@Dao
interface AnnuncioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(annuncio: Annuncio)

    @Update
    fun update(annuncio: Annuncio)

    @Delete
    fun delete(annuncio: Annuncio)

    @Query("SELECT * FROM annuncio")
    fun getAllAnnunci(): List<Annuncio>

}
