package com.wladkoshelev.metronome.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/** data-модель для сохранение плейлиста в БД. повторяет модель {[PlayListData]} */

@Entity(tableName = "play_list_table")
data class PlayListEntity(
    @PrimaryKey val id: String,
    val name: String,
    val songsIdList: List<String>
)