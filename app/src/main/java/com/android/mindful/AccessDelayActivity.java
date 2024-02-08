package com.android.mindful;

import androidx.appcompat.app.AppCompatActivity;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ProgressBar;

import java.util.Calendar;
import java.util.Map;

public class AccessDelayActivity extends AppCompatActivity {

    private ProgressBar delayProgressBar;
    public SharedPreferences preferences;

    public String TAG = "Access Delay Activity";

    private Button continueBtn, closeBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access_delay);
        preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        delayProgressBar = findViewById(R.id.delay_progress_bar);

        String packageName = getIntent().getStringExtra("app_package");

        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);

        // Get the current time in milliseconds
        Calendar calendar = Calendar.getInstance();
        long endMillis = calendar.getTimeInMillis();

        // Set the time to the beginning of the day (midnight)
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long startMillis = calendar.getTimeInMillis();

        Map<String, UsageStats> lUsageStatsMap = usageStatsManager.queryAndAggregateUsageStats(startMillis, endMillis);
        UsageStats usageStats = lUsageStatsMap.get(packageName);

        assert usageStats != null;
        long totalTimeUsageInMillis = usageStats.getTotalTimeInForeground();
        //
        long delayTime = calculateDelayTime((int) totalTimeUsageInMillis);
        Log.d(TAG, "Delay Time: " + delayTime);
        new CountDownTimer(delayTime, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Calculate the progress and update the ProgressBar
                int progress = (int) ((delayTime - millisUntilFinished) * 100 / delayTime);
                delayProgressBar.setProgress(progress);
            }

            @Override
            public void onFinish() {
                Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
                fadeIn.setDuration(500); // Adjust the duration of the fade-in animation as needed

                continueBtn.setVisibility(View.VISIBLE);
                continueBtn.startAnimation(fadeIn);
            }
        }.start();

        continueBtn = findViewById(R.id.continue_btn);
        continueBtn.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                long delayClosedAt = preferences.getLong("Delay_Activity_Closed_At", 0);
                long timeDifference = System.currentTimeMillis() - delayClosedAt;
                Log.d("Access Delay","Delay Closed Before: "+ String.valueOf(timeDifference));
                finishAndRemoveTask();
            } else {
                // For versions prior to Lollipop, use the traditional finish method
                finish();
            }
        });

        closeBtn = findViewById(R.id.close_btn);
        closeBtn.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAffinity();
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
            } else {
                // For versions prior to Lollipop, use the traditional finish method
                finish();
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
            }
        });



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong("Delay_Activity_Closed_At", System.currentTimeMillis());
    }
    public static long calculateDelayTime(int usageTime) {
        long usageTimeMs = usageTime * 1000;  // Convert usage time to milliseconds
        long delayTime;

        if (usageTimeMs < 600000) {  // Less than 10 minutes
            delayTime = 10000 + (long) (5000 * (usageTimeMs / 600000));  // Linear interpolation between 10s to 15s
        } else if (usageTimeMs < 3600000) {  // Less than 1 hour
            delayTime = 30000;  // 30s constant delay
        } else if (usageTimeMs < 21600000) {  // Less than 6 hours
            delayTime = 30000 + (long) (15000 * ((usageTimeMs - 3600000) / 18000000));  // Linear interpolation between 30s to 45s
        } else {
            delayTime = 60000;  // 60s constant delay
        }

        return delayTime;
    }
    @Override
    public void onBackPressed() {
        // Override the back button press to do nothing (disable going back)
    }
}