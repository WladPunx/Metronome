package com.wladkoshelev.metronome.di

import com.wladkoshelev.metronome.MetronomeLDS
import com.wladkoshelev.metronome.database.SongEntityMapper
import com.wladkoshelev.metronome.database.SongREP
import com.wladkoshelev.metronome.database.SongsDB
import com.wladkoshelev.metronome.database.SongsLDS
import com.wladkoshelev.metronome.ui.metronome.MetronomeVM
import com.wladkoshelev.metronome.ui.songs.AllSongsVM

object KoinModuleList {
    val list = listOf(

        MetronomeLDS().mModule(),
        MetronomeVM().mModule(),


        SongsDB().mModule(),
        SongEntityMapper().mModule(),
        SongsLDS().mModule(),
        SongREP().mModule(),


        AllSongsVM().mModule()
    )
}