package com.android.mindful.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.mindful.model.AppInfo;
import com.android.mindful.R;
import com.android.mindful.managers.ManageConfiguredApps;
import com.android.mindful.utils.SharedPrefUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CustomAdapter extends RecyclerView.Adapter<AppViewHolder>{

    private List<AppInfo> appInfoList;

    Context context;
    public CustomAdapter(List<AppInfo> appInfoList, Context context) {
        this.appInfoList = appInfoList;
        this.context = context;
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

        Set<String> configuredApps = ManageConfiguredApps.getTempConfiguredAppsList(context);
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

