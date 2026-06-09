package com.example.netarchive

import android.app.Application
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKTokenExpiredHandler
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NetArchiveApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            System.loadLibrary("sqlcipher")
            android.util.Log.d("SQLCipher", "Library loaded successfully")
        } catch (e: UnsatisfiedLinkError) {
            android.util.Log.e("SQLCipher", "Failed to load library", e)
        }
        VK.addTokenExpiredHandler(object : VKTokenExpiredHandler {
            override fun onTokenExpired() {
                VK.logout()
            }
        })
    }
}