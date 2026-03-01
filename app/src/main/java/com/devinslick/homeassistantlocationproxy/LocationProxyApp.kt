package com.devinslick.homeassistantlocationproxy

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.osmdroid.config.Configuration

@HiltAndroidApp
class LocationProxyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize OSMDroid once at app start rather than on every map recomposition.
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))
    }
}
