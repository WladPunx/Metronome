package com.wladkoshelev.metronome.database


data class PlayListData(
    val id: String,
    val name: String,
    val songsIdList: List<SongData>
)
