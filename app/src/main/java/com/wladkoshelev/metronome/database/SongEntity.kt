package com.wladkoshelev.metronome.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/** data-модель песни для БД
 *
повторяет модель {[SongData]}, но имеет {[date]} для сортировки песен по дате их создания

{[SongREP.Impl.allSongs]} {[SongREP.Impl.saveSong] }*/

@Entity(tableName = "songs_table")
data class SongEntity(
    @PrimaryKey val id: String,
    val name: String,
    val speed: Int,
    val tactSize: Int,
    val date: Long
)