package com.android.mindful.service;

import  android.accessibilityservice.AccessibilityServiceInfo;
import android.app.ActivityManager;
import android.app.TaskStackBuilder;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
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
    private static final String TAG = "Accessibility service";
    private SharedPrefUtils prefUtils;
    private final Handler handler = new Handler();

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (prefUtils == null) {
            prefUtils = new SharedPrefUtils(this);
        }

        String AccessibilityEventPackageName;

        // Log.d(TAG, "Accessibility Event!");
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (event.getPackageName() != null && event.getClassName() != null) {
                ComponentName componentName = new ComponentName(
                        event.getPackageName().toString(),
                        event.getClassName().toString()
                );
                ActivityInfo activityInfo = getActivityName(componentName);
                if (activityInfo != null) {
                    AccessibilityEventPackageName = activityInfo.packageName.trim();
                } else {
                    AccessibilityEventPackageName = "";
                }
            } else {
                AccessibilityEventPackageName = "";
            }
        } else {
            AccessibilityEventPackageName = "";
        }


        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);

        UsageEvents usageEvents = usageStatsManager.queryEvents(
                prefUtils.getLastAppCheckTime(), System.currentTimeMillis());

        while (usageEvents.hasNextEvent()) {
            UsageEvents.Event usageEvent = new UsageEvents.Event();
            usageEvents.getNextEvent(usageEvent);

            if (usageEvent.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED ) {
                String usageStatsPackageName = usageEvent.getPackageName();

                // Compare with Accessibility event package name

                Log.d(TAG, "Usage Event - Package Name: " + usageStatsPackageName);
                Log.d(TAG, "Accessibility Event - Package Name: " + AccessibilityEventPackageName);

                Log.d(TAG, "Last App Package: " + prefUtils.getLastAppPackage());

                Set<String> configuredApps = prefUtils.getConfiguredApps();

                Log.d(TAG, configuredApps.toString());

                if ((configuredApps.contains(AccessibilityEventPackageName) || configuredApps.contains(usageStatsPackageName)) &&
                        !configuredApps.contains(prefUtils.getLastAppPackage()) &&
                        !prefUtils.getLastAppPackage().equals("com.android.mindful")) {

                    Log.d(TAG, "Last App Package Before Delay: " + prefUtils.getLastAppPackage());
                    handler.postDelayed(() -> {
                        Log.d(TAG, "Usage Event:" + usageEvent.getEventType());
                        Log.d(TAG, "Accessbility Event"+  event.getEventType());

                        Intent intent = new Intent(AppAccessibilityService.this, AccessDelayActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.putExtra("app_package", (!AccessibilityEventPackageName.isEmpty() ? AccessibilityEventPackageName : usageStatsPackageName));
                        TaskStackBuilder.create(AppAccessibilityService.this)
                                .addNextIntentWithParentStack(intent)
                                .startActivities();

                    }, 50);
                }
                prefUtils.setLastAppPackage( (!AccessibilityEventPackageName.isEmpty() ? AccessibilityEventPackageName : usageStatsPackageName));
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
                 AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ;

        // Set the type of feedback your service provides.
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;

        info.notificationTimeout = 100;

        this.setServiceInfo(info);
        Log.d(TAG,"Service Connected");


    }

    public String getForegroundApp() {
        String currentApp = "NULL";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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


    private ActivityInfo getActivityName(ComponentName componentName) {
        try {
            return getPackageManager().getActivityInfo(componentName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            // e.printStackTrace();
            return null;
        }
    }



}
