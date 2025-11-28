package com.example.locationproxy.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

class LocationSpooferService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
