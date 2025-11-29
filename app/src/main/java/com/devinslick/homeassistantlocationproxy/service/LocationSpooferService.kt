package com.devinslick.homeassistantlocationproxy.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.devinslick.homeassistantlocationproxy.R
import com.devinslick.homeassistantlocationproxy.data.HaAttributes
import com.devinslick.homeassistantlocationproxy.data.SettingsProvider
import com.devinslick.homeassistantlocationproxy.network.HaRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Foreground service for polling Home Assistant and injecting mock locations.
 */
@AndroidEntryPoint
class LocationSpooferService : Service() {

    @Inject
    lateinit var settings: SettingsProvider

    @Inject
    lateinit var haRepository: HaRepository
    @Inject
    lateinit var settingsEditor: com.devinslick.homeassistantlocationproxy.data.SettingsEditor

    private val serviceJob = SupervisorJob()
    private val scope = CoroutineScope(serviceJob + Dispatchers.Default)

    @javax.inject.Inject
    lateinit var locationManager: LocationManager

    @javax.inject.Inject
    lateinit var notificationManager: NotificationManager

    private val CHANNEL_ID = "ha-spoofing-channel"
    private val NOTIFICATION_ID = 101

    override fun onCreate() {
        super.onCreate()
        // locationManager and notificationManager are injected by Hilt
        // Make sure the notification channel exists
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification("Initializing..."))

        scope.launch {
            settings.isPollingEnabled.collect { pollingEnabled ->
                if (pollingEnabled) startPollingLoop() else stopPollingLoop()
            }
        }

        return START_STICKY
    }

    private var pollingJob: Job? = null

    private fun startPollingLoop() {
        if (pollingJob?.isActive == true) return

        pollingJob = scope.launch {
            updateNotification("Polling enabled")
            var consecutiveUnauthorized = 0
            while (isActive && settings.isPollingEnabled.first()) {
                val pollingInterval = settings.pollingInterval.first()
                val isSpoofing = settings.isSpoofingEnabled.first()

                val result = haRepository.fetchEntityState()

                when (result) {
                    is com.devinslick.homeassistantlocationproxy.network.HaResult.Success -> {
                        val attrs = result.state.attributes
                        updateNotification("Last: ${attrs.latitude}, ${attrs.longitude}")
                        if (isSpoofing) {
                            try {
                                injectMockLocation(attrs)
                            } catch (se: SecurityException) {
                                updateNotification("Mocking unavailable: Permission")
                            } catch (e: Exception) {
                                updateNotification("Error: ${e.localizedMessage}")
                            }
                        }
                    }
                    is com.devinslick.homeassistantlocationproxy.network.HaResult.Failure -> {
                        val err = result.error
                        when (err) {
                            is com.devinslick.homeassistantlocationproxy.network.HaError.Unauthorized -> {
                                updateNotification("HA Error: Unauthorized — please check token")
                                // Increment counter, disable spoofing after repeated unauthorized errors
                                consecutiveUnauthorized++
                                if (consecutiveUnauthorized >= 3) {
                                    try {
                                        settingsEditor.setIsSpoofingEnabled(false)
                                    } catch (_: Exception) {
                                    }
                                }
                            }
                            is com.devinslick.homeassistantlocationproxy.network.HaError.NotFound -> {
                                updateNotification("HA Error: Entity not found")
                            }
                            is com.devinslick.homeassistantlocationproxy.network.HaError.Network -> {
                                updateNotification("Network error from HA: retrying")
                            }
                            else -> {
                                updateNotification("HA Error: ${err::class.simpleName}")
                            }
                        }
                    }
                }

                delay(pollingInterval * 1000L)
            }
            updateNotification("Polling stopped")
        }
    }

    private fun stopPollingLoop() {
        pollingJob?.cancel()
        pollingJob = null
    }

    private fun injectMockLocation(attrs: HaAttributes) {
        val lat = attrs.latitude ?: return
        val lon = attrs.longitude ?: return
        val alt = attrs.altitude ?: 0.0

        val fineGranted = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (!fineGranted) throw SecurityException("ACCESS_FINE_LOCATION not granted")

        val provider = LocationManager.GPS_PROVIDER
        val loc = Location(provider).apply {
            latitude = lat
            longitude = lon
            altitude = alt
            accuracy = 1f
            time = System.currentTimeMillis()
        }

        try {
            try {
                locationManager.addTestProvider(provider, false, false, false, false, true, true, true, android.location.Criteria.POWER_LOW, android.location.Criteria.ACCURACY_FINE)
            } catch (e: IllegalArgumentException) {
                // Could already exist or not be addable — ignore
            }
            locationManager.setTestProviderEnabled(provider, true)
            locationManager.setTestProviderStatus(provider, android.location.LocationProvider.AVAILABLE, null, System.currentTimeMillis())
            locationManager.setTestProviderLocation(provider, loc)
        } catch (se: SecurityException) {
            throw se
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "HA Location Spoofer", NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(contentText: String): Notification {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("HA Location Spoofer")
            .setContentText(contentText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
        return builder.build()
    }

    private fun updateNotification(text: String) {
        val notification = buildNotification(text)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopPollingLoop()
        scope.cancel()
        try {
            locationManager.removeTestProvider(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
            // ignore
        }
    }
}

