package com.example.tutormatch.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "calendario")
data class Calendario(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val data: Date?,
    val stato_pren: Boolean,
    val email_prof: String)
