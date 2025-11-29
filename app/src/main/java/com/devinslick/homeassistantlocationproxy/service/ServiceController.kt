package com.devinslick.homeassistantlocationproxy.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Controller for starting and stopping the `LocationSpooferService` in an app-consistent way.
 */
interface ServiceController {
    fun startService()
    fun stopService()
}

class ForegroundServiceController @Inject constructor(@ApplicationContext private val context: Context) : ServiceController {
    override fun startService() {
        val intent = Intent(context, LocationSpooferService::class.java)
        ContextCompat.startForegroundService(context, intent)
    }

    override fun stopService() {
        val intent = Intent(context, LocationSpooferService::class.java)
        context.stopService(intent)
    }
}
