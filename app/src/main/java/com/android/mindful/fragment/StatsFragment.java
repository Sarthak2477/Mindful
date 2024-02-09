   package com.android.mindful.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.android.mindful.model.AppStats;
import com.android.mindful.R;
import com.android.mindful.activity.ConfigureAppsActivity;
import com.android.mindful.managers.ManageAppStats;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

   public class StatsFragment extends Fragment {


    public StatsFragment() {
    }


    public static StatsFragment newInstance() {
        StatsFragment fragment = new StatsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);
        Button configureApps_btn = view.findViewById(R.id.configure_apps);

        configureApps_btn.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), ConfigureAppsActivity.class));
        });


//        RecyclerView recyclerView = view.findViewById(R.id.stat_recycler_view);
//        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
//        try {
//            recyclerView.setAdapter(new StatAppAdapter(prepareAppStatList()));
//        } catch (PackageManager.NameNotFoundException e) {
//            throw new RuntimeException(e);
//        }

        return view;

    }

    public List<AppStats> prepareAppStatList() throws PackageManager.NameNotFoundException {
        List<AppStats> appStatsList = new ArrayList<>();
        SharedPreferences preferences = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        Set<String> configuredApps = preferences.getStringSet("configuredApps",new HashSet<>());
        ApplicationInfo applicationInfo;
        for(String app : configuredApps){
            PackageManager packageManager = getActivity().getPackageManager();
            Drawable appIcon = packageManager.getApplicationIcon(app);
            applicationInfo = packageManager.getApplicationInfo(app, 0);
            String appName = (String) packageManager.getApplicationLabel(applicationInfo);
            List<BarEntry> barEntryList = new ArrayList<>();

            BarDataSet barDataSet = new BarDataSet(barEntryList, "Usage");
            String dailyAvg = convertMillisecondsToString(ManageAppStats.getDailyAverage());
            String compareLastWeek = ManageAppStats.getUsagePercentageChangeLastWeek(getActivity(), app) + "%";
            String weekStat = convertMillisecondsToString(ManageAppStats.getTotalScreenTimeForAppThisWeek(getActivity(), app));
            appStatsList.add(new AppStats(appIcon, appName, barDataSet, dailyAvg,weekStat, compareLastWeek));
        }

        return  appStatsList;
    }
       public String convertMillisecondsToString(long milliseconds) {
           long seconds = milliseconds / 1000;
           long hours = seconds / 3600;
           long minutes = (seconds % 3600) / 60;

           String hourMinuteString = String.format("%02dh %02dm", hours, minutes);
           return hourMinuteString;
       }
}