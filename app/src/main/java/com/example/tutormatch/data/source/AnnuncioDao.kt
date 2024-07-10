package com.example.tutormatch.data.source

import androidx.room.*
import com.example.tutormatch.data.model.Annuncio

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
