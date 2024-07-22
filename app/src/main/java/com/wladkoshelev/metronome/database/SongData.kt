package com.wladkoshelev.metronome.database

/** domain-модель для песни  */
data class SongData(
    /** уникальный рандомный ИД */
    val id: String,
    /** пользовательское имя */
    val name: String,
    /**  BMP-песни */
    val speed: Int,
    /** кол-во ударов в такте */
    val tactSize: Int
)