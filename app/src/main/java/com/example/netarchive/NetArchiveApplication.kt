package com.example.netarchive

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NetArchiveApplication() : Application() {

    override fun onCreate() {
        super.onCreate()
        try {
            System.loadLibrary("sqlcipher")
            android.util.Log.d("SQLCipher", "Library loaded successfully")
        } catch (e: UnsatisfiedLinkError) {
            android.util.Log.e("SQLCipher", "Failed to load library", e)
        }
    }
}