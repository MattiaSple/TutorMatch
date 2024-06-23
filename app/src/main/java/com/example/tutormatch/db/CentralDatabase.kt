package com.example.tutormatch.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Studenti::class], version=1)
abstract class CentralDatabase: RoomDatabase() {
    abstract fun studentiDao(): StudentiDao
    abstract fun tutorDao(): TutorDao
    abstract fun annunciDao(): AnnunciDao
    abstract fun calendarioDao(): CalendarioDao
    abstract fun chatDao(): ChatDao
    abstract fun prenotazioniDao(): PrenotazioniDao


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