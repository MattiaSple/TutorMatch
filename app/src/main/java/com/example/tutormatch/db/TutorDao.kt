package com.example.tutormatch.db

import androidx.room.*

@Dao
interface TutorDao {
    @Query("select * from studenti")
    fun getAllStudents(): List<Studenti>

    @Query("select * from studenti where matricola=:matricola")
    fun getStudenteByMatricola(matricola: Int): Studenti

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg student: Studenti)

    @Update
    fun update(studenti: Studenti)
    @Delete
    fun delete(studenti: Studenti)
}