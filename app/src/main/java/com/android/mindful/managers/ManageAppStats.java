package com.android.mindful.managers;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ManageAppStats {


    public static final String TAG = "ManageAppStats";
    public static List<Long> getDailyAppUsageInLastSevenDays(Context context, String packageName) {
        List<Long> dailyUsageList = new ArrayList<>();

        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        long startMillis = calendar.getTimeInMillis();
        long endMillis = System.currentTimeMillis();

        List<UsageStats> appUsageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startMillis, endMillis);

        for (int i = 0; i < 7; i++) {
            long usageTime = 0;
            for (UsageStats stats : appUsageStatsList) {
                if (stats.getPackageName().equals(packageName)) {
                    if (stats.getLastTimeUsed() >= startMillis && stats.getLastTimeUsed() < endMillis) {
                        usageTime = stats.getTotalTimeInForeground();
                        dailyUsageList.add(usageTime/60000);

                    }
                }
            }
            endMillis = startMillis;
            calendar.add(Calendar.DAY_OF_YEAR, -1);
            startMillis = calendar.getTimeInMillis();
        }
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
        Log.d(TAG, "total usage: "+totalUsageTime);
        Log.d(TAG, "last week: "+lastWeekUsageTime);
        long thisWeekUsageTime = totalUsageTime - lastWeekUsageTime;
        Log.d(TAG, "This Week" + thisWeekUsageTime);

        double usagePercentageChange = ((double) (thisWeekUsageTime - lastWeekUsageTime) / lastWeekUsageTime) * 100;
        Log.d(TAG, "usage percentage: " + usagePercentageChange);
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
        Log.d(TAG,"Start of Week: " + sdf.format(new Date(startOfWeek)));
        Log.d(TAG,"End of Week: " + sdf.format(new Date(endOfDay)));


        List<UsageStats> stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startOfWeek, endOfDay);

        for (UsageStats usageStats : stats) {
            if (usageStats.getPackageName().equals(packageName)) {
                totalScreenTime += usageStats.getTotalTimeInForeground();
            }
        }
        Log.d(TAG,"Total Screen Time: " + totalScreenTime);

        return totalScreenTime;

    }


}
