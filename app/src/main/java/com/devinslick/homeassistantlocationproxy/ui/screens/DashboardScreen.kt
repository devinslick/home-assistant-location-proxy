package com.devinslick.homeassistantlocationproxy.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.devinslick.homeassistantlocationproxy.ui.MainViewModel
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Text as MText
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import android.preference.PreferenceManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onOpenSettings: () -> Unit,
    onOpenAppSettings: () -> Unit = {},
    onOpenDevOptions: () -> Unit = {},
    onRequestLocationPermission: () -> Unit = {},
    onRequestNotificationPermission: () -> Unit = {},
    onOpenAutoStartSettings: () -> Unit = {},
    onOpenMaps: (Double, Double, String?) -> Unit = { _, _, _ -> }
) {
    val isPollingEnabled by viewModel.isPollingEnabled.collectAsState()
    val isSpoofingEnabled by viewModel.isSpoofingEnabled.collectAsState()
    val lastAttributes by viewModel.lastAttributes.collectAsState()
    val status by viewModel.statusMessage.collectAsState()

    // refresh permission state when entering this screen
    LaunchedEffect(Unit) {
        viewModel.refreshPermissions()
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column {
            CenterAlignedTopAppBar(title = { MText("HA Location Proxy") }, actions = {
                IconButton(onClick = { onOpenSettings() }) { Icon(Icons.Default.Map, contentDescription = "Open Settings") }
            })
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Top) {
            val hasLocationPermission by viewModel.hasLocationPermission.collectAsState()
            val hasNotificationPermission by viewModel.hasNotificationPermission.collectAsState()
            val isMockApp by viewModel.isMockLocationApp.collectAsState()

            if (!hasLocationPermission) {
                PermissionHintRow(text = "Location permission required for spoofing.", onClick = { onOpenAppSettings() }, buttonText = "Open App Settings")
                PermissionHintRow(text = "Request runtime location permission from the OS.", onClick = { onRequestLocationPermission() }, buttonText = "Request Location")
            }
            if (!hasNotificationPermission) {
                PermissionHintRow(text = "Notification permission required to show service notifications.", onClick = { onRequestNotificationPermission() }, buttonText = "Request Notifications")
            }
            if (!isMockApp) {
                PermissionHintRow(text = "Mock location app not selected. Open Developer Options and choose this app as mock location.", onClick = { onOpenDevOptions() }, buttonText = "Open Developer Options")
            }
            val needsAutoStart by viewModel.needsAutoStartPrompt.collectAsState()
            val hasBootCompleted by viewModel.hasBootCompletedPermission.collectAsState()
            if (needsAutoStart) {
                PermissionHintRow(text = "Device may block apps from auto-starting at boot; open Auto-Start settings to allow this app to run after reboot.", onClick = { onOpenAutoStartSettings() }, buttonText = "Open Auto-Start Settings")
            }
            if (!hasBootCompleted) {
                PermissionHintRow(text = "The RECEIVE_BOOT_COMPLETED permission is required to start at boot. If missing, open App Settings to review permissions.", onClick = { onOpenAppSettings() }, buttonText = "Open App Settings")
            }
            Text(text = "Status: $status")
            // Map - show a small map card centered on the last known location
            val lat = lastAttributes?.latitude
            val lon = lastAttributes?.longitude
            if (lat != null && lon != null) {
                val context = LocalContext.current
                // Initialize OSMDroid configuration
                org.osmdroid.config.Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))

                AndroidView(
                    factory = { ctx ->
                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            controller.setZoom(15.0)
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f) // Take available space
                        .padding(8.dp),
                    update = { mapView ->
                        val point = GeoPoint(lat, lon)
                        mapView.controller.setCenter(point)
                        mapView.overlays.clear()
                        val marker = Marker(mapView)
                        marker.position = point
                        marker.title = lastAttributes?.friendly_name ?: "Vehicle"
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        mapView.overlays.add(marker)
                        mapView.invalidate() // Redraw
                    }
                )
                
                androidx.compose.material3.Button(onClick = { 
                    onOpenMaps(lat, lon, lastAttributes?.friendly_name)
                }, modifier = Modifier.padding(8.dp)) {
                    Text(text = "Open in External Maps")
                }
            }
            Text(text = "Last location: ${lastAttributes?.latitude ?: "n/a"}, ${lastAttributes?.longitude ?: "n/a"}")
            val pollingInterval by viewModel.pollingInterval.collectAsState()
            Text(text = "Polling interval: $pollingInterval s")

            RowItem(label = "Enable Polling", checked = isPollingEnabled, onChecked = { viewModel.setPollingEnabled(it) })
            val canEnableSpoofing = isPollingEnabled && hasLocationPermission && isMockApp
            RowItem(label = "Enable Spoofing", checked = isSpoofingEnabled, enabled = canEnableSpoofing, onChecked = { viewModel.setSpoofingEnabled(it) })

            Button(onClick = { viewModel.refreshLatestState() }) {
                Text(text = "Refresh")
            }

            Button(onClick = { onOpenSettings() }) {
                Text(text = "Settings")
            }
        }
    }
  }
}

@Composable
fun RowItem(label: String, checked: Boolean, enabled: Boolean = true, onChecked: (Boolean) -> Unit) {
    androidx.compose.material3.ListItem(
        headlineContent = { Text(text = label) },
        trailingContent = { androidx.compose.material3.Switch(checked = checked, onCheckedChange = onChecked, enabled = enabled) }
    )
}

@Composable
fun PermissionHintRow(text: String, onClick: () -> Unit = {}, buttonText: String = "Open Settings") {
    androidx.compose.material3.Card(modifier = Modifier.padding(8.dp)) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = text)
            androidx.compose.material3.Button(onClick = onClick) {
                Text(text = buttonText)
            }
        }
    }
}
