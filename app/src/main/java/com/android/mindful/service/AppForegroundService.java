package com.android.mindful.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageEvents;
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
import com.android.mindful.utils.SharedPrefUtils;

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
        Log.d(TAG,"Detecting Configured Apps...");
        long startTime = prefUtils.getLastAppCheckTime();
        long endTime = System.currentTimeMillis();

        UsageEvents usageEvents = usageStatsManager.queryEvents(startTime, endTime);
        Log.d(TAG, usageEvents.toString());
        Log.d(TAG, "Events: " + usageEvents.hasNextEvent());
        while (usageEvents.hasNextEvent()) {
            UsageEvents.Event usageEvent = new UsageEvents.Event();
            usageEvents.getNextEvent(usageEvent);

            if (usageEvent.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED) {
                String packageName = usageEvent.getPackageName();

                Log.d(TAG, "Usage Event - Package Name: " + packageName);

                Set<String> configuredApps = prefUtils.getConfiguredApps();

                if (configuredApps.contains(packageName) &&
                        !configuredApps.contains(prefUtils.getLastAppPackage()) &&
                        !prefUtils.getLastAppPackage().equals("com.android.mindful")) {

                    Log.d(TAG, "Last App Package Before Delay: " + prefUtils.getLastAppPackage());

                    handler.postDelayed(() -> {
                        Intent i = new Intent(AppForegroundService.this, AccessDelayActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.putExtra("app_package", packageName);
                        startActivity(i);
                    }, 50);
                }

                prefUtils.setLastAppPackage(packageName);
                prefUtils.setLastAppCheckTime(System.currentTimeMillis());
            }
        }
    }


}
