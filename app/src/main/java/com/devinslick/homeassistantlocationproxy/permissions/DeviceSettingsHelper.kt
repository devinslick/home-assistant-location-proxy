package com.devinslick.homeassistantlocationproxy.permissions

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Helper to open device specific settings (auto-start / boot) for some OEMs.
 * This is best-effort — many manufacturers use different packages/activities and there's no stable API.
 */
class DeviceSettingsHelper @Inject constructor(@ApplicationContext private val context: Context) {
    private val pm = context.packageManager

    /**
     * Attempt to open a known OEM auto-start settings screen or fallback to Application details.
     * Returns true if any activity was launched.
     */
    fun openAutoStartSettings(): Boolean {
        // List of candidate intents for various OEMs — best-effort, may vary across versions
        val candidates = listOf(
            // Xiaomi / MIUI
            Intent().setComponent(ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
            Intent("miui.intent.action.APP_AUTO_START"),
            Intent().setComponent(ComponentName("com.miui.securitycenter", "com.miui.powerkeeper.ui.PowerKeeperActivity")),
            // Huawei
            Intent().setComponent(ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity")),
            // Oppo / ColorOS
            Intent().setComponent(ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
            Intent().setComponent(ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")),
            // Vivo
            Intent().setComponent(ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager")),
            Intent().setComponent(ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpPermissionActivity")),
            // Asus
            Intent().setComponent(ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.MainActivity"))
        )

        for (intent in candidates) {
            try {
                if (pm.resolveActivity(intent, 0) != null) {
                    // Ensure flags for launching outside of an Activity
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    return true
                }
            } catch (e: Exception) {
                // ignore and continue
            }
        }

        // Fallback: open application details settings
        return openAppDetails()
    }

    fun openAppDetails(): Boolean {
        return try {
            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = android.net.Uri.fromParts("package", context.packageName, null)
            intent.data = uri
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }
}
