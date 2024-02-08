package com.android.mindful.fragment;

import android.accessibilityservice.AccessibilityService;
import android.app.AppOpsManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.mindful.R;


public class GetStartedFragment extends Fragment {

    public GetStartedFragment() {
        // Required empty public constructor
    }

    public static GetStartedFragment newInstance() {
        return new GetStartedFragment();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_get_started, container, false);

        // Find the button by ID
        View getStartedButton = view.findViewById(R.id.get_started);

        // Set a click listener on the button
        getStartedButton.setOnClickListener(v -> {
            AppOpsManager appOpsManager = (AppOpsManager) requireActivity().getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), requireActivity().getPackageName());
            boolean usage_permission = mode == AppOpsManager.MODE_ALLOWED;

            if(!usage_permission || !isAccessibilityServiceEnabled(requireContext(), AccessibilityService.class)){
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, GrantPermissionFragment.newInstance())
                        .addToBackStack(null)
                        .commit();
            }
        });

        return view;
    }
    public boolean isAccessibilityServiceEnabled(Context context, Class<? extends AccessibilityService> serviceClass) {
        ComponentName expectedComponentName = new ComponentName(context, serviceClass);
        String enabledServicesSetting = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        return enabledServicesSetting != null && enabledServicesSetting.contains(expectedComponentName.flattenToString());
    }

}