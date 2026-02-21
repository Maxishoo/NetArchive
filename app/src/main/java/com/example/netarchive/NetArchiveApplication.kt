package com.example.netarchive

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NetArchiveApplication() : Application() {

    override fun onCreate() {
        super.onCreate()
    }
}