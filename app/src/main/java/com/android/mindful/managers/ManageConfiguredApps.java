package com.android.mindful.managers;

import static android.content.Context.USAGE_STATS_SERVICE;

import android.app.Activity;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.android.mindful.utils.SharedPrefUtils;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ManageConfiguredApps {
    private static Set<String> configuredApps = new HashSet<>();

    public static void addConfiguredApp(String packageName){
        System.out.println("Added App: " + packageName);
        configuredApps.add(packageName);

    }

    public static void removeConfiguredApp(String packageName){
        configuredApps.remove(packageName);

    }

    public static void commitAppList(Context context, Set<String> prevList){
        System.out.println("Set: " + configuredApps);
        Set<String> combinedList = new HashSet<>(prevList);
        combinedList.addAll(configuredApps);
        new SharedPrefUtils(context).setConfiguredApps(combinedList);

    }

    public static String getAppNameFromPackageInfo(PackageManager packageManager, ApplicationInfo appInfo) {
        CharSequence appName = packageManager.getApplicationLabel(appInfo);
        return appName.toString();
    }

    public static String getForegroundTimeForPackage(String packageName, Activity activity) {
        UsageStatsManager usageStatsManager = (UsageStatsManager) activity.getSystemService(USAGE_STATS_SERVICE);

        // Get the current time in milliseconds
        Calendar calendar = Calendar.getInstance();
        long endMillis = calendar.getTimeInMillis();

        // Set the time to the beginning of the day (midnight)
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long startMillis = calendar.getTimeInMillis();

        Map<String, UsageStats> lUsageStatsMap = usageStatsManager.queryAndAggregateUsageStats(startMillis, endMillis);

        UsageStats usageStats = lUsageStatsMap.get(packageName);
        if (usageStats != null) {
            long totalTimeUsageInMillis = usageStats.getTotalTimeInForeground();
            // Convert milliseconds to hours, minutes, and seconds
            long seconds = totalTimeUsageInMillis / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;

            // Calculate remaining minutes and seconds
            minutes %= 60;
            seconds %= 60;

            // Format the result
            return String.format("%02d hrs %02d mins %02d secs", hours, minutes, seconds);
        } else {
            // Handle the case when the package name is not found in the map
            return "Not available";
        }
    }


}
