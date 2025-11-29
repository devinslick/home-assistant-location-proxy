package com.devinslick.homeassistantlocationproxy.permissions

import android.app.AppOpsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PermissionChecker @Inject constructor(@ApplicationContext private val context: Context) {

    fun hasFineLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    fun isMockLocationAppSelected(): Boolean {
        try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            // OPSTR_MOCK_LOCATION available on API 29+, but we target 34 so it's safe to use
            val mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_MOCK_LOCATION, Process.myUid(), context.packageName)
            return mode == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            // If the check is unavailable / throws, assume not selected
            return false
        }
    }

    fun hasReceiveBootCompletedPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECEIVE_BOOT_COMPLETED) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    /**
     * Some OEMs prevent apps from auto-starting on boot; present a prompt for known manufacturers.
     */
    fun needsAutoStartPrompt(): Boolean {
        val manu = Build.MANUFACTURER.lowercase().trim()
        return manu in setOf("xiaomi", "huawei", "oppo", "vivo", "letv", "lenovo", "asus")
    }
}
