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


    public boolean isBooleanAppBlockHandlerRunning = false;

    private Intent homeIntent;

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

        homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

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
//        Log.d(TAG,"Detecting Configured Apps...");
        long startTime = prefUtils.getLastAppCheckTime();
        long endTime = System.currentTimeMillis();

        UsageEvents usageEvents = usageStatsManager.queryEvents(startTime, endTime);


        while (usageEvents.hasNextEvent()) {
            UsageEvents.Event usageEvent = new UsageEvents.Event();
            usageEvents.getNextEvent(usageEvent);
            String packageName = usageEvent.getPackageName();

            Handler appBlockHandler =  new Handler(getMainLooper());
            Set<String> configuredApps = prefUtils.getConfiguredApps();

            if (usageEvent.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED) {


                Runnable runnable = () -> {
                    Log.d(TAG, "Current App: " + getCurrentApp());
                    if(getCurrentApp().equals(packageName)){
                        startActivity(homeIntent);
                        Intent intent = new Intent(AppForegroundService.this, RestricWindow.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                    isBooleanAppBlockHandlerRunning = false;
                };


                if(isBooleanAppBlockHandlerRunning){
                    Log.d(TAG, "handler stopped");
                    appBlockHandler.removeCallbacks(runnable);
                    isBooleanAppBlockHandlerRunning = false;

                }
                if(configuredApps.contains(packageName)){

                    prefUtils.setLastOpenedTime(packageName, System.currentTimeMillis());
                    long delay = 0;
                    HashMap<String, Long> appTimerList = prefUtils.getAppTimer();
                    long appTime = prefUtils.getAppTimer().get(packageName);


                    if(appTimerList.containsKey(packageName) && appTime != 0){


                        long currTime = System.currentTimeMillis();
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);
                        long  dayBeginTime = calendar.getTimeInMillis();

                        long usageTime = ManageAppStats.getUsageStatistics(this,packageName, dayBeginTime, currTime).timeInForeground;
                        Log.d(TAG, "Usage time: " + usageTime);
                        delay = appTime - usageTime;

                        Log.d(TAG, "App timer: " + appTime);
                        Log.d(TAG, "App will close after " + delay/1000);
                        if(delay > 0){
                            Log.d(TAG, "postdelayed called for " + delay + " millis");
                            isBooleanAppBlockHandlerRunning = true;
                            appBlockHandler.postDelayed(runnable, delay);
                        }else if (delay < 0){
                            startActivity(homeIntent);
                            Intent intent = new Intent(AppForegroundService.this, RestricWindow.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    }

                    if (!configuredApps.contains(prefUtils.getLastAppPackage())
                            && !prefUtils.getLastAppPackage().equals("com.android.mindful")
                            && !RestricWindow.active
                            && delay >= 0
                            ) {
                            if(isBooleanAppBlockHandlerRunning){
                                Log.d(TAG, "handler stopped");
                                appBlockHandler.removeCallbacks(runnable);
                                isBooleanAppBlockHandlerRunning = false;
                            }
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

    public String getCurrentApp(){
        String currentApp = "";
        long currentTime = System.currentTimeMillis();
        List<UsageStats> usageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, currentTime - 60000, currentTime);

        // Get the package name of the currently running app from the UsageStats object.
        if (usageStats != null && !usageStats.isEmpty()) {
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
