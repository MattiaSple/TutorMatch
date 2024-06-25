package com.example.tutormatch.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Prenotazione::class, Annuncio::class, Calendario::class, Utente::class, Chat::class], version=1)
abstract class CentralDatabase: RoomDatabase() {
    abstract fun UtenteDao(): UtenteDao
    abstract fun AnnuncioDao(): AnnuncioDao
    abstract fun CalendarioDao(): CalendarioDao
    abstract fun PrenotazioneDao(): PrenotazioneDao
    abstract fun ChatDao(): ChatDao


    companion object {
        @Volatile
        private var INSTANCE: CentralDatabase? = null

        fun getInstance(context: Context): CentralDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    CentralDatabase::class.java, "student_database"
                ).allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}