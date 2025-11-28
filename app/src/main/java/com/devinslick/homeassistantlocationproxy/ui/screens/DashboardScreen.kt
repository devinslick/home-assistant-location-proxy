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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.devinslick.homeassistantlocationproxy.ui.MainViewModel
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onOpenSettings: () -> Unit,
    onOpenAppSettings: () -> Unit = {},
    onOpenDevOptions: () -> Unit = {}
) {
    val isPollingEnabled by viewModel.isPollingEnabled.collectAsState()
    val isSpoofingEnabled by viewModel.isSpoofingEnabled.collectAsState()
    val lastAttributes by viewModel.lastAttributes.collectAsState()
    val status by viewModel.statusMessage.collectAsState()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Top) {
            val hasLocationPermission by viewModel.hasLocationPermission.collectAsState()
            val hasNotificationPermission by viewModel.hasNotificationPermission.collectAsState()
            val isMockApp by viewModel.isMockLocationApp.collectAsState()

            if (!hasLocationPermission) {
                PermissionHintRow(text = "Location permission required for spoofing. Click to open app settings.", onClick = { onOpenAppSettings() })
            }
            if (!isMockApp) {
                PermissionHintRow(text = "Mock location app not selected. Open Developer Options and choose this app as mock location.", onClick = { onOpenDevOptions() })
            }
            Text(text = "Status: $status")
            Text(text = "Last location: ${lastAttributes?.latitude ?: "n/a"}, ${lastAttributes?.longitude ?: "n/a"}")
            Text(text = "Polling interval: ${viewModel.pollingInterval.collectAsState().value} s")

            RowItem(label = "Enable Polling", checked = isPollingEnabled, onChecked = { viewModel.setPollingEnabled(it) })
            RowItem(label = "Enable Spoofing", checked = isSpoofingEnabled, enabled = isPollingEnabled, onChecked = { viewModel.setSpoofingEnabled(it) })

            Button(onClick = { viewModel.refreshLatestState() }) {
                Text(text = "Refresh")
            }

            Button(onClick = { onOpenSettings() }) {
                Text(text = "Settings")
            }
        }
    }
}

@Composable
fun RowItem(label: String, checked: Boolean, enabled: Boolean = true, onChecked: (Boolean) -> Unit) {
    androidx.compose.material3.ListItem {
        Text(text = label)
        Switch(checked = checked, onCheckedChange = onChecked, enabled = enabled)
    }
}

@Composable
fun PermissionHintRow(text: String, onClick: () -> Unit = {}) {
    androidx.compose.material3.Card(modifier = Modifier.padding(8.dp)) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = text)
            androidx.compose.material3.Button(onClick = onClick) {
                Text(text = "Open Settings")
            }
        }
    }
}
