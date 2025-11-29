package com.devinslick.homeassistantlocationproxy

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.devinslick.homeassistantlocationproxy.di.BootEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class BootReceiver : BroadcastReceiver() {
    private val logTag = "BootReceiver"

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            // Access Hilt components via entry point since BroadcastReceiver isn't injected
            val entryPoint = EntryPointAccessors.fromApplication(context.applicationContext, BootEntryPoint::class.java)
            val settingsRepo = entryPoint.settingsRepository()

            // Launch a coroutine to check settings and start the service if polling is enabled.
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    val shouldPoll = settingsRepo.isPollingEnabled.first()
                    if (shouldPoll) {
                        val serviceIntent = Intent(context, com.devinslick.homeassistantlocationproxy.service.LocationSpooferService::class.java)
                        try {
                            ContextCompat.startForegroundService(context.applicationContext, serviceIntent)
                        } catch (e: Exception) {
                            Log.w(logTag, "Failed to start LocationSpooferService on boot: ${e.localizedMessage}")
                        }
                    }
                } catch (e: Exception) {
                    Log.w(logTag, "Error checking polling preference on boot: ${e.localizedMessage}")
                }
            }
        }
    }
}
