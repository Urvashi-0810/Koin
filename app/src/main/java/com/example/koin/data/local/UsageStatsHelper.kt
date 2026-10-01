package com.example.koin.data.local

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import com.example.koin.data.model.AppCategory
import com.example.koin.data.model.AppCategoryType
import java.util.Calendar

object UsageStatsHelper {

    /**
     * Checks whether the user has granted Usage Access permission.
     */
    fun hasUsagePermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /**
     * Returns an intent to open System Settings for Usage Access permission.
     */
    fun getUsagePermissionIntent(): Intent {
        return Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
    }

    /**
     * Queries daily app usage stats from midnight until now and classifies apps.
     */
    fun getTodayAppUsages(
        context: Context,
        customOverrides: Map<String, AppCategoryType> = emptyMap()
    ): List<AppCategory> {
        if (!hasUsagePermission(context)) {
            return emptyList()
        }

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val usageStatsList: List<UsageStats> = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        ) ?: emptyList()

        val pm = context.packageManager
        val appMap = mutableMapOf<String, AppCategory>()

        for (stats in usageStatsList) {
            val totalTimeInForegroundMs = stats.totalTimeInForeground
            val minutes = totalTimeInForegroundMs / (1000 * 60)
            if (minutes <= 0) continue

            val packageName = stats.packageName
            // Skip system UI / launcher / self
            if (packageName == context.packageName ||
                packageName.contains("systemui") ||
                packageName.contains("launcher")
            ) {
                continue
            }

            val appName = getAppName(pm, packageName)
            val categoryType = customOverrides[packageName] ?: DefaultAppClassifier.getCategoryType(packageName)

            val existing = appMap[packageName]
            val existingMins = existing?.rawUsageMinutes ?: 0L
            val totalMins = existingMins + minutes

            appMap[packageName] = AppCategory(
                packageName = packageName,
                appName = appName,
                categoryType = categoryType,
                rawUsageMinutes = totalMins,
                isCustomizedByParent = customOverrides.containsKey(packageName)
            )
        }

        return appMap.values.sortedByDescending { it.rawUsageMinutes }
    }

    private fun getAppName(pm: PackageManager, packageName: String): String {
        return try {
            val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getApplicationInfo(packageName, 0)
            }
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName.substringAfterLast('.')
        }
    }
}
