   package com.android.mindful.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.mindful.adapters.StatAppAdapter;
import com.android.mindful.model.AppStats;
import com.android.mindful.R;
import com.android.mindful.activity.ConfigureAppsActivity;
import com.android.mindful.managers.ManageAppStats;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
        FloatingActionButton configureApps_btn = view.findViewById(R.id.configure_apps);
        TextView noAppsMsg = view.findViewById(R.id.text_no_apps_configured);
        List<AppStats> appStatsList = prepareAppStatList();

        configureApps_btn.setOnClickListener(v->{
            Intent intent = new Intent(getContext(), ConfigureAppsActivity.class);
            startActivity(intent);
        });
        if(!appStatsList.isEmpty()){
            RecyclerView recyclerView = view.findViewById(R.id.stat_recycler_view);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.setAdapter(new StatAppAdapter(getActivity(),appStatsList));
        }else{
            noAppsMsg.setVisibility(View.VISIBLE);
        }

        return view;

    }

    public List<AppStats> prepareAppStatList(){
        List<AppStats> appStatsList = new ArrayList<>();
        SharedPreferences preferences = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        Set<String> configuredApps = preferences.getStringSet("configuredApps",new HashSet<>());
        ApplicationInfo applicationInfo;
        for(String app : configuredApps){
            try{
                PackageManager packageManager = getActivity().getPackageManager();
                Drawable appIcon = packageManager.getApplicationIcon(app);
                applicationInfo = packageManager.getApplicationInfo(app, 0);
                String appName = (String) packageManager.getApplicationLabel(applicationInfo);
                List<BarEntry> barEntryList = new ArrayList<>();

                List <Long> dailyUsageList = ManageAppStats.getDailyAppUsageInLastSevenDays(getActivity(), applicationInfo.packageName);
                Log.d("Stats Fragment","List Size: " + dailyUsageList.size() );
                int i = 0;
                for(Long dayUsage: dailyUsageList){
                    barEntryList.add(new BarEntry((float) i, (float) dayUsage));
                    i++;
                }

                BarDataSet barDataSet = new BarDataSet(barEntryList, "Usage");

                long sum = 0, avg = 0;
                for(long usage : dailyUsageList){
                    sum += usage;
                }
                if(!dailyUsageList.isEmpty())
                    avg = sum /dailyUsageList.size();
                Log.d("StatsFragment", "Average: " + avg);
                String dailyAvg = "00m";
                if (avg < 60) {
                     dailyAvg = String.format("%dm", avg);
                } else {
                    long hours = avg / 60;
                    long remainingMinutes = avg % 60;
                    dailyAvg =  String.format("%dh %02dm", hours, remainingMinutes);
                }

                double percentageChange = ManageAppStats.getUsagePercentageChangeLastWeek(getActivity(), app);
                String compareLastWeek = "0%";
                if(percentageChange > 0){
                    compareLastWeek = "+" + percentageChange+ "%";
                } else {
                    compareLastWeek =  percentageChange+ "%";
                }

                String packageName = applicationInfo.packageName;
                String weekStat = convertMillisecondsToString(ManageAppStats.getTotalScreenTimeForAppThisWeek(getActivity(), app));
                appStatsList.add(new AppStats(appIcon, appName, packageName, barDataSet, dailyAvg,weekStat, compareLastWeek));
            }catch (PackageManager.NameNotFoundException e){
                Log.d("Stats", "Package Not Found: " + app);
            }
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