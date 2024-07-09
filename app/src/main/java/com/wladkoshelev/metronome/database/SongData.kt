package com.wladkoshelev.metronome.database

data class SongData(
    val id: String,
    val name: String,
    val speed: Int,
    val tactSize: Int
)