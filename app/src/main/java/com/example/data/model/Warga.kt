package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "warga")
data class Warga(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String = "",
    val roomNumber: String = "",
    val checkInDate: String = ""
)
