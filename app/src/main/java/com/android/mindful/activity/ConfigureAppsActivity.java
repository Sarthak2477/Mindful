package com.android.mindful.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.mindful.appinfo.AppInfo;
import com.android.mindful.R;
import com.android.mindful.adapters.CustomAdapter;
import com.android.mindful.managers.ManageConfiguredApps;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConfigureAppsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CustomAdapter adapter;
    private ProgressBar progressBar;
    private ExecutorService executorService;

    private Button btnDone;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_apps);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnDone = findViewById(R.id.btnDone);
        btnDone.setOnClickListener(v -> {
            SharedPreferences preferences = getSharedPreferences("AppPrefs",MODE_PRIVATE);
            Set<String> appList = preferences.getStringSet("configuredApps", new HashSet<>());

            SharedPreferences.Editor editor = preferences.edit();
            ManageConfiguredApps.commitAppList(editor, appList);
            Log.d("Configured Apps ", appList.toString());
            finish();
        });
        progressBar = findViewById(R.id.progressBar);

        // Initialize ExecutorService with a fixed number of threads
        executorService = Executors.newFixedThreadPool(2);

        // Load installed apps and their usage stats asynchronously using ExecutorService
        executorService.submit(new LoadAppsTask());
    }

    private class LoadAppsTask implements Runnable {
        @Override
        public void run() {
            // Show the ProgressBar on the main thread before the task starts
            runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE));

            List<AppInfo> appInfoList = getInstalledApps();

            // Hide the ProgressBar on the main thread once the task is complete
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                adapter = new CustomAdapter(appInfoList, getSharedPreferences("AppPrefs",MODE_PRIVATE));
                recyclerView.setAdapter(adapter);

            });
        }
    }

    private List<AppInfo> getInstalledApps() {
        List<AppInfo> installedAppsList = new ArrayList<>();

// Get the PackageManager
        PackageManager packageManager = getPackageManager();

// Create an Intent for main activities labeled as launchers
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

// Query for main activities
        List<ResolveInfo> resolvedInfos = packageManager.queryIntentActivities(mainIntent, 0);

// Get the package names of user-facing apps
        Set<String> userAppsSet = new HashSet<>();
        for (ResolveInfo resolveInfo : resolvedInfos) {
            userAppsSet.add(resolveInfo.activityInfo.packageName);
        }

// Get a list of all installed applications
        List<ApplicationInfo> installedApplications = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo appInfo : installedApplications) {
            // Check if the app is a user-facing app
            if (userAppsSet.contains(appInfo.packageName)) {
                // Get the app name using the method introduced before
                String appName = ManageConfiguredApps.getAppNameFromPackageInfo(packageManager, appInfo);
                Drawable appIcon = packageManager.getApplicationIcon(appInfo);
                String stats = ManageConfiguredApps.getForegroundTimeForPackage(appInfo.packageName, this);

                // Add the package name to the list
                installedAppsList.add(new AppInfo(appName, stats, appIcon, appInfo.packageName));
            }
        }

        return installedAppsList;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Shutdown the ExecutorService when the activity is destroyed
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }


}
