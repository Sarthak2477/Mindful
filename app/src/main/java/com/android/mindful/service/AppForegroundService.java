package com.android.mindful.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.android.mindful.R;
import com.android.mindful.activity.AccessDelayActivity;
import com.android.mindful.activity.RestricWindow;
import com.android.mindful.utils.SharedPrefUtils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class AppForegroundService extends Service {

    private static final String CHANNEL_ID = "ForegroundServiceChannel";
    private static final int NOTIFICATION_ID = 1;
    private static final String TAG = "AccessibilityService";
    private SharedPrefUtils prefUtils;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private UsageStatsManager usageStatsManager;

    public AppForegroundService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        Notification notification = createNotification();
        startForeground(NOTIFICATION_ID, notification);

        if (prefUtils == null) {
            prefUtils = new SharedPrefUtils(this);
        }

        // Schedule a task to check app usage periodically
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                detectApp();
                handler.postDelayed(this, TimeUnit.SECONDS.toMillis(1)); // Schedule the task again
            }
        }, TimeUnit.SECONDS.toMillis(1));

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Remove any callbacks or clean-up tasks
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Mindful")
                .setContentText("Monitoring for configured apps..")
                .setSmallIcon(R.mipmap.app_icon)
                .build();
    }



    private void detectApp() {
//        Log.d(TAG,"Detecting Configured Apps...");
        long startTime = prefUtils.getLastAppCheckTime();
        long endTime = System.currentTimeMillis();

        UsageEvents usageEvents = usageStatsManager.queryEvents(startTime, endTime);

        Log.d(TAG, usageEvents.toString());
//        Log.d(TAG, "Events: " + usageEvents.hasNextEvent());

        while (usageEvents.hasNextEvent()) {
            UsageEvents.Event usageEvent = new UsageEvents.Event();
            usageEvents.getNextEvent(usageEvent);
            String packageName = usageEvent.getPackageName();

//
            if (usageEvent.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED) {



//                Log.d(TAG, "Usage Event - Package Name: " + packageName);

                Set<String> configuredApps = prefUtils.getConfiguredApps();

                if (configuredApps.contains(packageName) &&
                        !configuredApps.contains(prefUtils.getLastAppPackage()) &&
                        !prefUtils.getLastAppPackage().equals("com.android.mindful")) {



                        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                        homeIntent.addCategory(Intent.CATEGORY_HOME);
                        homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    if(doesAppUsageExceeds(packageName)){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            if(usageStatsManager.getAppStandbyBucket() == UsageStatsManager.STANDBY_BUCKET_ACTIVE){
                                startActivity(homeIntent);
                                Intent intent = new Intent(AppForegroundService.this, RestricWindow.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        }
                    }else{
                        startActivity(homeIntent);
                        Intent i = new Intent(AppForegroundService.this, AccessDelayActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.putExtra("app_package", packageName);
                        startActivity(i);
                    }
                }

                prefUtils.setLastAppPackage(packageName);
                prefUtils.setLastAppCheckTime(System.currentTimeMillis());
            }
        }
    }

    public boolean doesAppUsageExceeds(String packageName) {
        HashMap<String, Long> appTimerList = prefUtils.getAppTimer();
        long totalTimeUsageInMillis = 0;
        double setTime = 0;
        Log.d(TAG, "App timer List: " + appTimerList);
        Log.d(TAG, "package: " + packageName);

        if (appTimerList.containsKey(packageName)) {
            setTime = appTimerList.get(packageName);

            UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);

            Calendar calendar = Calendar.getInstance();
            long endMillis = calendar.getTimeInMillis();

            // Set the time to the beginning of the day (midnight)
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            long startMillis = calendar.getTimeInMillis();

            // Query usage stats for the given package
            List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY, endMillis-1000, endMillis);

            // Find the usage stats for the specific package
            for (UsageStats usageStats : usageStatsList) {
                if (packageName.equals(usageStats.getPackageName())) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        totalTimeUsageInMillis = usageStats.getTotalTimeVisible();
                        Log.d(TAG, "Total usage Time: " + totalTimeUsageInMillis);
                    } else {
                        totalTimeUsageInMillis = usageStats.getTotalTimeInForeground();
                    }

                    // Check if app is visible
                    return (setTime < totalTimeUsageInMillis);
                }
            }
        }
        return false;
    }


}
