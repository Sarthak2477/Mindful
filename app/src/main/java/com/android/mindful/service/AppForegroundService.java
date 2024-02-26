package com.android.mindful.service;

import static android.util.Log.d;

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
import com.android.mindful.managers.ManageAppStats;
import com.android.mindful.model.AppUsageInfo;
import com.android.mindful.utils.SharedPrefUtils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

public class AppForegroundService extends Service {

    private static final String CHANNEL_ID = "ForegroundServiceChannel";
    private static final int NOTIFICATION_ID = 1;
    private static final String TAG = "AccessibilityService";
    private SharedPrefUtils prefUtils;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private UsageStatsManager usageStatsManager;


    Handler appBlockHandler = new Handler(getMainLooper());


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
                .setContentText("Monitoring for configured apps")
                .setSmallIcon(R.mipmap.app_icon)
                .build();
    }



    private void detectApp() {
        long startTime = prefUtils.getLastAppCheckTime();
        long endTime = System.currentTimeMillis();
        UsageEvents usageEvents = usageStatsManager.queryEvents(startTime, endTime);

        while (usageEvents.hasNextEvent()) {
            UsageEvents.Event usageEvent = new UsageEvents.Event();
            usageEvents.getNextEvent(usageEvent);

            String packageName = usageEvent.getPackageName();
            Set<String> configuredApps = prefUtils.getConfiguredApps();

            switch (usageEvent.getEventType()) {
                case UsageEvents.Event.ACTIVITY_RESUMED:
                    handleActivityResumed(packageName, configuredApps);
                    break;

                case UsageEvents.Event.ACTIVITY_STOPPED:
                case UsageEvents.Event.ACTIVITY_PAUSED:
                    handleActivityStoppedOrPaused(packageName, configuredApps);
                    break;
            }
        }
    }

    private void handleActivityResumed(String packageName, Set<String> configuredApps) {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (configuredApps.contains(packageName)) {
            prefUtils.setLastOpenedTime(packageName, System.currentTimeMillis());
            long delay = 0;
            HashMap<String, Long> appTimerList = prefUtils.getAppTimer();

            if (appTimerList.containsKey(packageName)) {
                long currTime = System.currentTimeMillis();
                long dayBeginTime = getDayBeginTime(currTime);

                long usageTime = ManageAppStats.getUsageStatistics(this, packageName, dayBeginTime, currTime).timeInForeground;
                Log.d(TAG, "Usage time: " + usageTime);

                delay = appTimerList.get(packageName) - usageTime;
                Log.d(TAG, "App will close after " + delay / 1000);

                Log.d(TAG, "App timer: " + appTimerList.get(packageName));

                if (delay > 0) {
                    scheduleAppBlock(packageName, appBlockHandler, delay);
                } else {
                    startActivity(new Intent(AppForegroundService.this, RestricWindow.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                }
            }

            handleConfiguredApp(packageName, configuredApps, homeIntent, delay);
        }

        updatePreferences(packageName);
    }

    private void handleConfiguredApp(String packageName, Set<String> configuredApps, Intent homeIntent, long delay) {
        if (!configuredApps.contains(prefUtils.getLastAppPackage()) &&
                !prefUtils.getLastAppPackage().equals("com.android.mindful") &&
                !RestricWindow.active &&
                delay > 0) {
            startActivity(homeIntent);
            Intent i = new Intent(AppForegroundService.this, AccessDelayActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra("app_package", packageName);
            startActivity(i);
        }
    }

    private void handleActivityStoppedOrPaused(String packageName, Set<String> configuredApps) {
        if (configuredApps.contains(packageName)) {
            appBlockHandler.removeCallbacksAndMessages(null);
        }
    }

    private void scheduleAppBlock(String packageName, Handler appBlockHandler, long delay) {
        appBlockHandler.postDelayed(() -> {
            if (getCurrentApp().equals(packageName)) {
                Intent intent = new Intent(AppForegroundService.this, RestricWindow.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }, delay);
    }

    private void updatePreferences(String packageName) {
        prefUtils.setLastAppPackage(packageName);
        prefUtils.setLastAppCheckTime(System.currentTimeMillis());
    }

    private long getDayBeginTime(long currTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }



    public String getCurrentApp(){
        String currentApp = "";
        long currentTime = System.currentTimeMillis();
        List<UsageStats> usageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, currentTime - 10000, currentTime);

        // Get the package name of the currently running app from the UsageStats object.
        if (usageStats != null && usageStats.size() > 0) {
            SortedMap<Long, UsageStats> mySortedMap = new TreeMap<>();
            for (UsageStats usageStat : usageStats) {
                mySortedMap.put(usageStat.getLastTimeUsed(), usageStat);
            }
            if (!mySortedMap.isEmpty()) {
                currentApp = Objects.requireNonNull(mySortedMap.get(mySortedMap.lastKey())).getPackageName();
            }
        }
        return currentApp;
    }




}
