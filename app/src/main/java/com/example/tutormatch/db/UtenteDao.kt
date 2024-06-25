package com.example.tutormatch.db

import androidx.room.*

@Dao
interface UtenteDao {
    @Insert
    suspend fun inserisci(utente: Utente)

    @Update
    suspend fun update(utente: Utente)

    @Delete
    suspend fun delete(utente: Utente)

    @Query("SELECT * FROM utente WHERE email = :email")
    suspend fun getUtenteByEmail(email: String): Utente?

    @Query("SELECT * FROM utente")
    suspend fun getAllUtenti(): List<Utente>

    //@Query("UPDATE utente SET nome = :nome, cognome = :cognome, password = :password, residenza = :residenza, via = :via, numero = :numero, ruolo = :ruolo WHERE email = :email")
    //suspend fun aggiornaUtente(email: String, nome: String, cognome: String, password: String, residenza: String, via: String, numero: Int, ruolo: Boolean)
}
