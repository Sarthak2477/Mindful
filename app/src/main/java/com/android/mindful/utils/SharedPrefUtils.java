package com.android.mindful.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.mindful.model.Task;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SharedPrefUtils {

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Context context;

    private static final String KEY_LAST_APP_PACKAGE = "lastAppPackage";
    private final String TASK_LIST_KEY = "task_list";


    private String PREF_TAG = "AppPrefs";
    public SharedPrefUtils(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences(PREF_TAG, Context.MODE_PRIVATE);
        this.editor = preferences.edit();
    }

    public void setFirstLaunch(boolean isFirstLaunch){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("first_launch", isFirstLaunch);
        editor.apply();
        editor.commit();
    }

    public boolean isFirstLaunch(){
        return preferences.getBoolean("first_launch", true);
    }

    public Set<String> getConfiguredApps(){
        return preferences.getStringSet("configuredApps", new HashSet<>());
    }

    public void setConfiguredApps(Set<String> configuredApps){
        editor.putStringSet("configuredApps", configuredApps);
        editor.apply();
    }

    public long getLastAppCheckTime(){
        long currentTime = System.currentTimeMillis();
        return  preferences.getLong("lastAppCheckTime", System.currentTimeMillis()-1000);
    }

    public void setLastAppCheckTime(long time){
        editor.putLong("lastAppCheckTime", time);
        editor.apply();
    }

    public void setLastAppPackage(String packageName) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_LAST_APP_PACKAGE, packageName);
        editor.apply();
    }

    public String getLastAppPackage() {
        return preferences.getString(KEY_LAST_APP_PACKAGE, null);
    }

    // Convert a list to a JSON string
    public void saveTaskList(List<Task> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(TASK_LIST_KEY, json);
        editor.apply();
    }


    // Retrieve a list from a JSON string
    public List<Task> getTaskList() {
        Gson gson = new Gson();
        String json = preferences.getString(TASK_LIST_KEY, null);

        if (json != null) {
            Type type = new TypeToken<List<Task>>(){}.getType();
            return gson.fromJson(json, type);
        } else {
            return new ArrayList<>();
        }
    }


}
