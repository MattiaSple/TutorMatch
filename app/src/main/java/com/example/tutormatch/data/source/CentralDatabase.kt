package com.example.tutormatch.data.source

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.tutormatch.data.model.Annuncio
import com.example.tutormatch.data.model.Calendario
import com.example.tutormatch.data.model.Chat
import com.example.tutormatch.data.model.Prenotazione
import com.example.tutormatch.data.model.Utente

@Database(entities = [Prenotazione::class, Annuncio::class, Calendario::class, Utente::class, Chat::class], version=1)
abstract class CentralDatabase: RoomDatabase() {
    abstract fun utenteDao(): UtenteDao
    abstract fun annuncioDao(): AnnuncioDao
    abstract fun calendarioDao(): CalendarioDao
    abstract fun prenotazioneDao(): PrenotazioneDao
    //abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: CentralDatabase? = null

        fun getInstance(context: Context): CentralDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    CentralDatabase::class.java, "central_database"
                ).allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}