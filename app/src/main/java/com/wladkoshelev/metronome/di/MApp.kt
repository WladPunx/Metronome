package com.wladkoshelev.metronome.di

import android.app.Application
import org.koin.core.context.startKoin

class MApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            modules(KoinModuleList.list)
        }
    }
}