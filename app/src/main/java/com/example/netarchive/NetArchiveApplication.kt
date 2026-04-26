package com.example.netarchive



import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory

import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class NetArchiveApplication : Application() {


    override fun onCreate() {
        super.onCreate()
    }
}