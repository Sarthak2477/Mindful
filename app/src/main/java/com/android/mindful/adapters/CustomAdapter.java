package com.android.mindful.adapters;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.mindful.AppInfo;
import com.android.mindful.adapters.AppViewHolder;
import com.android.mindful.R;
import com.android.mindful.managers.ManageConfiguredApps;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CustomAdapter extends RecyclerView.Adapter<AppViewHolder>{

    private List<AppInfo> appInfoList;
    private SharedPreferences preferences;
    public CustomAdapter(List<AppInfo> appInfoList, SharedPreferences preferences) {
        this.appInfoList = appInfoList;
        this.preferences = preferences;
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app, parent, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        String appName = appInfoList.get(position).getAppName();
        String appStat = appInfoList.get(position).getUsageStat();
        Drawable icon = appInfoList.get(position).getAppIcon();
        String packageName = appInfoList.get(position).getPackageName();

        holder.appIcon.setImageDrawable(icon);
        holder.appName.setText(appName);
        holder.stat.setText(appStat);

        Set<String> configuredApps = preferences.getStringSet("configuredApps", new HashSet<>());
        System.out.println("Customer Adapter list: " + configuredApps);

        boolean isConfigured = configuredApps.contains(packageName);
        holder.box.setChecked(isConfigured);

        holder.box.setOnClickListener(v -> {
            if(holder.box.isChecked()){
                ManageConfiguredApps.addConfiguredApp(packageName);
            }else{
                ManageConfiguredApps.removeConfiguredApp(packageName);
            }
        });

    }

    @Override
    public int getItemCount() {
        return appInfoList.size();
    }


}

