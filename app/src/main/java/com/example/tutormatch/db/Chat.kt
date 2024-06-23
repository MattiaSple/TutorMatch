package com.example.tutormatch.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat")
data class Chat(
    @PrimaryKey val matricola: Int,
    val name: String,
    val surname: String,
    val course: String,
    val subscription_date: String)
