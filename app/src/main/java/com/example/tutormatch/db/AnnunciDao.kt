package com.example.tutormatch.db

import androidx.room.*

@Dao
interface AnnunciDao {
    @Query("select * from annunci")
    fun getAllStudents(): List<Annunci>

    @Query("select * from annunci where matricola=:matricola")
    fun getStudenteByMatricola(matricola: Int): Annunci

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg annuncio: Annunci)

    @Update
    fun update(annunci: Annunci)
    @Delete
    fun delete(annunci: Annunci)
}