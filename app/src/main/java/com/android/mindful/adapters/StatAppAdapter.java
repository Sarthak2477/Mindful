package com.android.mindful.adapters;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.mindful.model.AppStats;
import com.android.mindful.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class StatAppAdapter extends RecyclerView.Adapter<StatAppViewHolder> {
    List<AppStats> appStatsList;

    public StatAppAdapter(List<AppStats> appStatsList) {
        this.appStatsList = appStatsList;
    }

    @NonNull
    @Override
    public StatAppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stat_app, parent, false);
        return new StatAppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatAppViewHolder holder, int position) {
        Drawable appIcon = appStatsList.get(position).getAppIcon();
        holder.appIcon.setImageDrawable(appIcon);
        // You may also need to set other views in the ViewHolder with corresponding data from appStatsList
        holder.appName.setText(appStatsList.get(position).getAppName());
        holder.dailyAvg.setText(appStatsList.get(position).getDailyAvg());
        holder.weekStat.setText(appStatsList.get(position).getWeekStat());
        holder.comparedLastWeek.setText(appStatsList.get(position).getCompareLastWeek());

        // Update bar chart if needed
    }


    @Override
    public int getItemCount() {
        return appStatsList.size() ;
    }

    public List<Long> getDailyAppUsageInLastSevenDays(Context context, String packageName) {
        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        long startMillis = calendar.getTimeInMillis();
        long endMillis = System.currentTimeMillis();

        List<UsageStats> appUsageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startMillis, endMillis);

        List<Long> dailyUsageList = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            long usageTime = 0;
            for (UsageStats stats : appUsageStatsList) {
                if (stats.getPackageName().equals(packageName)) {
                    if (stats.getLastTimeUsed() >= startMillis && stats.getLastTimeUsed() < endMillis) {
                        usageTime += stats.getTotalTimeInForeground();
                    }
                }
            }
            dailyUsageList.add(usageTime);
            endMillis = startMillis;
            calendar.add(Calendar.DAY_OF_YEAR, -1);
            startMillis = calendar.getTimeInMillis();
        }
        return dailyUsageList;
    }
}
