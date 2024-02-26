package com.android.mindful.model;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

public class AppUsageInfo {
    Drawable appIcon;
    String appName, packageName;
    public long timeInForeground;
    public int launchCount;

    public AppUsageInfo(String pName) {
        this.packageName=pName;
    }

    @NonNull
    @Override
    public String toString() {
        return "AppUsageInfo{" +
                "appIcon=" + appIcon +
                ", appName='" + appName + '\'' +
                ", packageName='" + packageName + '\'' +
                ", timeInForeground=" + timeInForeground +
                ", launchCount=" + launchCount +
                '}';
    }
}
