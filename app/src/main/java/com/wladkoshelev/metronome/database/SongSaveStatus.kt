package com.wladkoshelev.metronome.database

/** статус сохранение песни или плейлиста
 *
{[SongREP.Impl.saveSong]} {[SongREP.Impl.savePlayList]} */

enum class SongSaveStatus {
    SUCCESS,
    EMPTY_NAME,
    NAME_EXIST;
}