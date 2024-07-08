package com.wladkoshelev.metronome.di

import com.wladkoshelev.metronome.MetronomeLDS
import com.wladkoshelev.metronome.ui_metranome.MetronomeVM
import org.koin.core.module.Module

object KoinModuleList {
    val list = listOf<Module>(

        MetronomeLDS().mModule(),

        MetronomeVM().mModule()


    )
}