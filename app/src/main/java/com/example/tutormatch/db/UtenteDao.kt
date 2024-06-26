package com.example.tutormatch.db

import androidx.room.*

@Dao
interface UtenteDao {
    @Insert
    fun insert(utente: Utente)

    @Update
    fun update(utente: Utente)

    @Delete
    fun delete(utente: Utente)

    @Query("SELECT * FROM utente WHERE email = :email")
    fun getUtenteByEmail(email: String): Utente?

    @Query("SELECT * FROM utente")
    fun getAllUtenti(): List<Utente>

    //@Query("UPDATE utente SET nome = :nome, cognome = :cognome, password = :password, residenza = :residenza, via = :via, numero = :numero, ruolo = :ruolo WHERE email = :email")
    //suspend fun aggiornaUtente(email: String, nome: String, cognome: String, password: String, residenza: String, via: String, numero: Int, ruolo: Boolean)
}
