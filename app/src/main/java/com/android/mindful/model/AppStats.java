package com.android.mindful.model;

import android.graphics.drawable.Drawable;

import com.github.mikephil.charting.data.BarDataSet;


public class AppStats {
    private Drawable appIcon;
    private  String appName;
    private String packageName;

    private BarDataSet barDataSet;
    private String dailyAvg;
    private String weekStat;
    private String compareLastWeek;

    public AppStats(Drawable appIcon, String appName, String packageName, BarDataSet barDataSet, String dailyAvg, String compareLastWeek) {
        this.appIcon = appIcon;
        this.appName = appName;
        this.packageName = packageName;
        this.barDataSet = barDataSet;
        this.dailyAvg = dailyAvg;
        this.weekStat = weekStat;
        this.compareLastWeek = compareLastWeek;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public BarDataSet getBarDataSet() {
        return barDataSet;
    }

    public void setBarDataSet(BarDataSet barDataSet) {
        this.barDataSet = barDataSet;
    }

    public String getDailyAvg() {
        return dailyAvg;
    }

    public void setDailyAvg(String dailyAvg) {
        this.dailyAvg = dailyAvg;
    }

    public String getWeekStat() {
        return weekStat;
    }

    public void setWeekStat(String weekStat) {
        this.weekStat = weekStat;
    }

    public String getCompareLastWeek() {
        return compareLastWeek;
    }

    public void setCompareLastWeek(String compareLastWeek) {
        this.compareLastWeek = compareLastWeek;
    }
}
