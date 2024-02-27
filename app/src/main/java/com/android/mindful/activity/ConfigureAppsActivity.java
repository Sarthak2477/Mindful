package com.android.mindful.activity;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.mindful.R;
import com.android.mindful.adapters.CustomAdapter;
import com.android.mindful.listeners.ScrollListener;
import com.android.mindful.managers.ManageConfiguredApps;
import com.android.mindful.model.AppInfo;
import com.android.mindful.utils.SharedPrefUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

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
            SharedPrefUtils prefUtils = new SharedPrefUtils(this);
            Set<String> appList = prefUtils.getConfiguredApps();
            ManageConfiguredApps.commitAppList(this, appList);
            Log.d("Configured Apps ", appList.toString());
            getOnBackPressedDispatcher().onBackPressed();
        });

        progressBar = findViewById(R.id.progressBar);

        // Initialize ExecutorService with a fixed number of threads
        executorService = Executors.newFixedThreadPool(12);

        recyclerView.addOnScrollListener(new ScrollListener((RecyclerView.LayoutManager) recyclerView.getLayoutManager()) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Log.d("ConfigureAppsActvity", "Load more..");
            }
        });

        // Load installed apps and their usage stats asynchronously using AsyncTask
        new LoadAppsTask().execute();
    }

    private class LoadAppsTask extends AsyncTask<Void, Void, List<AppInfo>> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<AppInfo> doInBackground(Void... params) {
            return getInstalledApps();
        }

        @Override
        protected void onPostExecute(List<AppInfo> appInfoList) {
            progressBar.setVisibility(View.GONE);
            adapter = new CustomAdapter(appInfoList, getBaseContext());
            recyclerView.setAdapter(adapter);
        }
    }

    private List<AppInfo> getInstalledApps() {
        SharedPrefUtils prefUtils = new SharedPrefUtils(this);
        List<AppInfo> installedAppsList = new ArrayList<>();

        PackageManager packageManager = getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolvedInfo = packageManager.queryIntentActivities(mainIntent, 0);

        SparseArray<Boolean> userAppsSet = new SparseArray<>();
        for (ResolveInfo resolveInfo : resolvedInfo) {
            userAppsSet.put(resolveInfo.activityInfo.packageName.hashCode(), true);
        }

        List<ApplicationInfo> installedApplications = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        // Use CompletableFuture for asynchronous processing

        // Join CompletableFuture results
        CompletableFuture.allOf(
                installedApplications.stream()
                        .filter(appInfo -> userAppsSet.get(appInfo.packageName.hashCode()) != null)
                        .map(appInfo -> CompletableFuture.runAsync(() -> {
                            String appName = ManageConfiguredApps.getAppNameFromPackageInfo(packageManager, appInfo);
                            Drawable appIcon = packageManager.getApplicationIcon(appInfo);
                            String stats = ManageConfiguredApps.getForegroundTimeForPackage(appInfo.packageName, this);

                            if (prefUtils.getConfiguredApps().contains(appInfo.packageName)) {
                                installedAppsList.add(0, new AppInfo(appName, stats, appIcon, appInfo.packageName));
                            } else {
                                installedAppsList.add(new AppInfo(appName, stats, appIcon, appInfo.packageName));
                            }
                        }, executorService)).toArray(CompletableFuture[]::new)
        ).join();

        return installedAppsList;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Shutdown the ExecutorService when the activity is destroyed
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
    }
}
