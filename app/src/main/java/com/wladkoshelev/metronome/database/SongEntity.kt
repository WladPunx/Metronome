package com.wladkoshelev.metronome.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs_table")
data class SongEntity(
    @PrimaryKey val id: String,
    val name: String,
    val speed: Int,
    val tactSize: Int,
    val date: Long
)