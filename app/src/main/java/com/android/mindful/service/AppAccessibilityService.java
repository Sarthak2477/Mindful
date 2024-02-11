package com.android.mindful.service;

import  android.accessibilityservice.AccessibilityServiceInfo;
import android.app.ActivityManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.android.mindful.activity.AccessDelayActivity;
import com.android.mindful.utils.SharedPrefUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;


public class AppAccessibilityService extends android.accessibilityservice.AccessibilityService {
    private static final String TAG = "Accessibilty service";
    private static final String PREFS_NAME = "AppPrefs";
    private SharedPrefUtils prefUtils;
    private final Handler handler = new Handler();

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (prefUtils == null) {
            prefUtils = new SharedPrefUtils(this);
        }




        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);

        UsageEvents usageEvents = usageStatsManager.queryEvents(
               prefUtils.getLastAppCheckTime(), System.currentTimeMillis());

        while (usageEvents.hasNextEvent()) {
            UsageEvents.Event usageEvent = new UsageEvents.Event();
            usageEvents.getNextEvent(usageEvent);
            Log.d( TAG, "Event:"+ usageEvent.getEventType());
            if (usageEvent.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED ) {
                String packageName  = usageEvent.getPackageName();

                Log.d(TAG, "Usage Event - Package Name: " + packageName);
                Log.d(TAG, "Usage Event - Package Name: " + usageEvent.getEventType());

                Log.d(TAG, "Last App Package: " + prefUtils.getLastAppPackage());
                Set<String> configuredApps = prefUtils.getConfiguredApps();
                Log.d(TAG, configuredApps.toString());
                Log.d(TAG, String.valueOf(event.getEventType()));

                if(configuredApps.contains(packageName) && !configuredApps.contains(prefUtils.getLastAppPackage()) && !prefUtils.getLastAppPackage().equals("com.android.mindful")){
                    Log.d(TAG, "Last App Package Before Delay: " + prefUtils.getLastAppPackage());
//                    long delayClosedAt = preferences.getLong("Delay_Activity_Closed_At", 0);
//                    long timeDifference = System.currentTimeMillis() - delayClosedAt;
//                    if(timeDifference >= 2000){
                        handler.postDelayed(() -> {
                            Intent intent = new Intent(AppAccessibilityService.this, AccessDelayActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("app_package", packageName);
                            startActivity(intent);
                        }, 50);
//                    }

                }
                prefUtils.setLastAppPackage(packageName);
                prefUtils.setLastAppCheckTime(System.currentTimeMillis());
            }
        }

    }

    @Override
    public void onInterrupt() {

    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        // Set the type of events that this service wants to listen to. Others
        // aren't passed to this service.
        info.eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED |
                AccessibilityEvent.TYPE_VIEW_FOCUSED;



        // Set the type of feedback your service provides.
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;



        info.notificationTimeout = 100;

        this.setServiceInfo(info);
        Log.d(TAG,"Service Connected");


    }




    public String getForegroundApp() {
        String currentApp = "NULL";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time);
            if (appList != null && appList.size() > 0) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                for (UsageStats usageStats : appList) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (mySortedMap != null && !mySortedMap.isEmpty()) {
                    currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                }
            }
        } else {
            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> tasks = am.getRunningAppProcesses();
            currentApp = tasks.get(0).processName;
        }

        return currentApp;
    }




}
