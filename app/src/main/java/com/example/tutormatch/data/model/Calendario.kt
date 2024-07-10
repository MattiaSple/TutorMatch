package com.example.tutormatch.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calendario")
data class Calendario(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val data: String,
    val stato_pren: Boolean,
    val email_prof: String)
