package com.android.mindful.managers;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.android.mindful.model.AppUsageInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        for (int i = 0; i < 7; i++) {
            // Move back one day


            long startMillis = calendar.getTimeInMillis(); // Set startMillis to the beginning of the current day

            // Set endMillis to the beginning of the next day
            long endMillis = startMillis + TimeUnit.DAYS.toMillis(1);

            AppUsageInfo appUsageInfo = getUsageStatistics(context, packageName, startMillis, endMillis);
            if(appUsageInfo != null)
                dailyUsageList.add(appUsageInfo.timeInForeground/60000);

            calendar.add(Calendar.DAY_OF_YEAR, -1);

        }

        while(dailyUsageList.size() < 7)
            dailyUsageList.add(0L);

        Collections.reverse(dailyUsageList);


        return dailyUsageList;
    }


    public static double getUsagePercentageChangeLastWeek(Context context, String packageName) {
        Calendar calendar = Calendar.getInstance();

        long endTime = calendar.getTimeInMillis();

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        calendar.add(Calendar.WEEK_OF_YEAR, -1);
        long startTime = calendar.getTimeInMillis();

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        long lastWeekEnd = calendar.getTimeInMillis();

        AppUsageInfo appUsageInfo = getUsageStatistics(context, packageName, startTime, endTime);
        long totalUsageTime = appUsageInfo.timeInForeground;

        AppUsageInfo lastWeekAppUsageInfo = getUsageStatistics(context, packageName, startTime, lastWeekEnd);
        long lastWeekUsageTime = lastWeekAppUsageInfo.timeInForeground;

        if (lastWeekUsageTime == 0) {
            return 0.0; // To avoid division by zero if the app was not used last week
        }

        long thisWeekUsageTime = totalUsageTime - lastWeekUsageTime;

        double usagePercentageChange = ((double) (thisWeekUsageTime - lastWeekUsageTime) / lastWeekUsageTime) * 100;
        return Math.round(usagePercentageChange * 100.0) / 100.0;
    }



    public static long getTotalScreenTimeForAppThisWeek(Context context, String packageName) {
        long totalScreenTime = 0;
        AppUsageInfo appUsageInfo = null;
        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        long startOfWeek = calendar.getTimeInMillis();
        long endOfDay = System.currentTimeMillis();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        while(appUsageInfo == null)
            appUsageInfo = getUsageStatistics(context, packageName, startOfWeek, endOfDay);

        List<UsageStats> stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startOfWeek, endOfDay);

        for (UsageStats usageStats : stats) {
            if (usageStats.getPackageName().equals(packageName)) {
                totalScreenTime += usageStats.getTotalTimeInForeground();
            }
        }

        return totalScreenTime;

    }

    public static AppUsageInfo getUsageStatistics(Context context, String packageName, long startTime, long endTime) {

        UsageEvents.Event currentEvent;
        List<UsageEvents.Event> allEvents = new ArrayList<>();
        HashMap<String, AppUsageInfo> map = new HashMap<>();

        UsageStatsManager mUsageStatsManager =  (UsageStatsManager)
                context.getSystemService(Context.USAGE_STATS_SERVICE);

        assert mUsageStatsManager != null;
        UsageEvents usageEvents = mUsageStatsManager.queryEvents(startTime, endTime);

//capturing all events in a array to compare with next element

        while (usageEvents.hasNextEvent()) {
            currentEvent = new UsageEvents.Event();
            usageEvents.getNextEvent(currentEvent);
            if (currentEvent.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND ||
                    currentEvent.getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND) {
                allEvents.add(currentEvent);
                String key = currentEvent.getPackageName();
// taking it into a collection to access by package name
                if (map.get(key)==null)
                    map.put(key,new AppUsageInfo(key));
            }
        }

//iterating through the arraylist
        for (int i=0;i<allEvents.size()-1;i++){
            UsageEvents.Event E0=allEvents.get(i);
            UsageEvents.Event E1=allEvents.get(i+1);

//for launchCount of apps in time range
            if (!E0.getPackageName().equals(E1.getPackageName()) && E1.getEventType()==1){
// if true, E1 (launch event of an app) app launched
                Objects.requireNonNull(map.get(E1.getPackageName())).launchCount++;
            }

//for UsageTime of apps in time range
            if (E0.getEventType()==1 && E1.getEventType()==2
                    && E0.getClassName().equals(E1.getClassName())){
                long diff = E1.getTimeStamp()-E0.getTimeStamp();
                Objects.requireNonNull(map.get(E0.getPackageName())).timeInForeground+= diff;
            }
        }
//transferred final data into modal class object
        return map.get(packageName);

    }

}
