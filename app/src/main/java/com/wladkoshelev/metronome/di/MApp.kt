package com.wladkoshelev.metronome.di

import android.app.Application

class MApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MKoin.start(this)
    }
}