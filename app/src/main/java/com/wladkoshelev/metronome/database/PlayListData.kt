package com.wladkoshelev.metronome.database


/** domain-модель для плейлиста */
data class PlayListData(
    /** уникальный рандомный ИД */
    val id: String,
    /** пользовательское имя */
    val name: String,
    /** список ИДшников песен {[SongData.id]} */
    val songsIdList: List<SongData>
)
