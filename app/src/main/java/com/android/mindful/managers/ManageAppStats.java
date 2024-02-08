package com.android.mindful.managers;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ManageAppStats {

    public static List<Long> dailyUsageList = new ArrayList<>();

    public static long getDailyAverage(){
        long sum = 0;

        if(dailyUsageList == null){
            return 0;
        }
        if(dailyUsageList.isEmpty()){
            return 0;
        }
        for(long usage : dailyUsageList){
            sum += usage;
        }

        return (long) sum/dailyUsageList.size();
    }

    public static List<Long> getDailyAppUsageInLastSevenDays(Context context, String packageName) {
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
                        usageTime += stats.getTotalTimeInForeground();
                    }
                }
            }
            dailyUsageList.add(usageTime);
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
        calendar.add(Calendar.WEEK_OF_YEAR, -1);
        long startTime = calendar.getTimeInMillis();

        List<UsageStats> statsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);

        long totalUsageTime = 0;
        long lastWeekUsageTime = 0;

        for (UsageStats stats : statsList) {
            if (stats.getPackageName().equals(packageName)) {
                totalUsageTime += stats.getTotalTimeInForeground();
                if (stats.getLastTimeUsed() >= startTime) {
                    lastWeekUsageTime += stats.getTotalTimeInForeground();
                }
            }
        }

        if (lastWeekUsageTime == 0) {
            return 0.0; // To avoid division by zero if app was not used last week
        }

        double usagePercentageChange = ((double) (totalUsageTime - lastWeekUsageTime) / lastWeekUsageTime) * 100;
        return usagePercentageChange;
    }

    public static long getTotalScreenTimeForAppThisWeek(Context context, String packageName) {
        long totalScreenTime = 0;
        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        long startOfWeek = calendar.getTimeInMillis();
        long endOfDay = System.currentTimeMillis();

        List<UsageStats> stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startOfWeek, endOfDay);

        for (UsageStats usageStats : stats) {
            if (usageStats.getPackageName().equals(packageName)) {
                totalScreenTime += usageStats.getTotalTimeInForeground();
            }
        }

        return totalScreenTime;
    }


}
