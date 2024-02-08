package com.android.mindful.managers;

import android.accessibilityservice.AccessibilityService;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.ComponentName;
import android.content.Context;
import android.provider.Settings;

public class ManagePermissions {
    public static boolean isAccessibilityServiceEnabled(Context context, Class<? extends AccessibilityService> serviceClass) {
        ComponentName expectedComponentName = new ComponentName(context, serviceClass);
        String enabledServicesSetting = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        return enabledServicesSetting != null && enabledServicesSetting.contains(expectedComponentName.flattenToString());
    }

    public static boolean isUsagePermissionGranted(Activity activity){
        AppOpsManager appOpsManager = (AppOpsManager) activity.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), activity.getPackageName());
        return  mode == AppOpsManager.MODE_ALLOWED;
    }
}
