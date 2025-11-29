package com.devinslick.homeassistantlocationproxy

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.tooling.preview.Preview
// hiltViewModel not used here; using Activity scoped ViewModel via viewModels()
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.devinslick.homeassistantlocationproxy.ui.MainViewModel
import androidx.activity.viewModels
import com.devinslick.homeassistantlocationproxy.ui.screens.DashboardScreen
import com.devinslick.homeassistantlocationproxy.permissions.DeviceSettingsHelper
import javax.inject.Inject
import com.devinslick.homeassistantlocationproxy.ui.screens.SettingsScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    @Inject
    lateinit var deviceSettingsHelper: DeviceSettingsHelper
    @Inject
    lateinit var serviceController: com.devinslick.homeassistantlocationproxy.service.ServiceController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val requestLocationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            // Refresh permissions in ViewModel after request finishes
            mainViewModel.refreshPermissions()
        }
        val requestNotificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            mainViewModel.refreshPermissions()
        }
        setContent {
            val navController = rememberNavController()
            val isPollingEnabled by mainViewModel.isPollingEnabled.collectAsState()
            LaunchedEffect(isPollingEnabled) {
                if (isPollingEnabled) serviceController.startService() else serviceController.stopService()
            }

            MaterialTheme {
                Surface {
                    NavHost(navController = navController, startDestination = "dashboard") {
                        composable("dashboard") {
                            DashboardScreen(
                                viewModel = mainViewModel,
                                onOpenSettings = { navController.navigate("settings") },
                                onOpenAppSettings = {
                                    val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    val uri = android.net.Uri.fromParts("package", packageName, null)
                                    intent.data = uri
                                    startActivity(intent)
                                },
                                onRequestLocationPermission = {
                                    requestLocationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                                },
                                onRequestNotificationPermission = {
                                    requestNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                },
                                onOpenDevOptions = {
                                    val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                                    startActivity(intent)
                                }
                                ,
                                onOpenAutoStartSettings = {
                                    // Use DeviceSettingsHelper to open OEM auto-start settings or fallback to app details
                                    deviceSettingsHelper.openAutoStartSettings()
                                },
                                onOpenMaps = { lat, lon, label ->
                                    val uri = android.net.Uri.parse("geo:${lat},${lon}?q=${lat},${lon}(${label ?: "location"})")
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                                    intent.setPackage("com.google.android.apps.maps")
                                    try {
                                        startActivity(intent)
                                    } catch (_: Exception) {
                                        // Fallback: open chooser
                                        val chooser = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                                        chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                        startActivity(chooser)
                                    }
                                }
                            )
                        }
                        composable("settings") {
                            SettingsScreen(viewModel = mainViewModel, onClose = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh permission states when Activity resumes
        mainViewModel.refreshPermissions()
    }
    
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name, Location Proxy App")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Greeting("Android")
}
