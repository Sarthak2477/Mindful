package com.android.mindful.managers;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ManageAppStats {


    public static final String TAG = "ManageAppStats";
    public static List<Long> getDailyAppUsageInLastSevenDays(Context context, String packageName) {
        List<Long> dailyUsageList = new ArrayList<>();

        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        for (int i = 0; i < 7; i++) {
            // Move back one day
            calendar.add(Calendar.DAY_OF_YEAR, -1);

            long startMillis = calendar.getTimeInMillis(); // Set startMillis to the beginning of the current day

            // Set endMillis to the beginning of the next day
            long endMillis = startMillis + TimeUnit.DAYS.toMillis(1);

            List<UsageStats> appUsageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startMillis, endMillis);

            long usageTime = 0;
            for (UsageStats stats : appUsageStatsList) {
                if (stats.getPackageName().equals(packageName)) {
                    usageTime = stats.getTotalTimeInForeground();
                    dailyUsageList.add(usageTime / 60000);
                    Log.d(TAG, packageName + " " + sdf.format(new Date(startMillis)) + " " + ((int)usageTime/60000));
                    break; // No need to continue checking other stats for the same day
                }
            }
        }

        Collections.reverse(dailyUsageList); // Reverse the list to get stats in chronological order
        return dailyUsageList;
    }


    public static double getUsagePercentageChangeLastWeek(Context context, String packageName) {
        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        Calendar calendar = Calendar.getInstance();

        long endTime = calendar.getTimeInMillis();

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        calendar.add(Calendar.WEEK_OF_YEAR, -1);
        long startTime = calendar.getTimeInMillis();

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        long lastWeekEnd = calendar.getTimeInMillis();


        List<UsageStats> statsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);

        long totalUsageTime = 0;
        long lastWeekUsageTime = 0;

        for (UsageStats stats : statsList) {
            if (stats.getPackageName().equals(packageName)) {
                totalUsageTime += stats.getTotalTimeInForeground();
                if (stats.getLastTimeUsed() >= startTime && stats.getLastTimeUsed() <= lastWeekEnd) {
                    lastWeekUsageTime += stats.getTotalTimeInForeground();
                }
            }
        }

        if (lastWeekUsageTime == 0) {
            return 0.0; // To avoid division by zero if the app was not used last week
        }
        long thisWeekUsageTime = totalUsageTime - lastWeekUsageTime;

        double usagePercentageChange = ((double) (thisWeekUsageTime - lastWeekUsageTime) / lastWeekUsageTime) * 100;
        return Math.round(usagePercentageChange * 100.0) / 100.0;
    }


    public static long getTotalScreenTimeForAppThisWeek(Context context, String packageName) {
        long totalScreenTime = 0;
        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        long startOfWeek = calendar.getTimeInMillis();
        long endOfDay = System.currentTimeMillis();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");


        List<UsageStats> stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startOfWeek, endOfDay);

        for (UsageStats usageStats : stats) {
            if (usageStats.getPackageName().equals(packageName)) {
                totalScreenTime += usageStats.getTotalTimeInForeground();
            }
        }

        return totalScreenTime;

    }


}
