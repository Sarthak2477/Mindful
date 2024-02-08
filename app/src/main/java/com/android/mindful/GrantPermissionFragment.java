package com.android.mindful;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.android.mindful.managers.ManagePermissions;

public class GrantPermissionFragment extends Fragment {


    public GrantPermissionFragment(){

    }
    public static GrantPermissionFragment newInstance() {
        return new GrantPermissionFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_grant_permission, container, false);

        Button usage_access_btn = view.findViewById(R.id.usage_access_btn);
        Button accessibility_service_btn = view.findViewById((R.id.accessibility_service_permissions_btn));
        Button continue_btn = view.findViewById(R.id.continue_btn);
        continue_btn.setEnabled(false);

        continue_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), MainActivity.class);
                startActivity(intent);
                requireActivity().finish();
            }
        });
        ActivityResultLauncher<Intent> usageAccessLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        System.out.println("Usage Access Permission Granted");

                    }
                });

        ActivityResultLauncher<Intent> accessibilityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        System.out.println("Accessibility Service Permission Granted");
                    }
                });

        usage_access_btn.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            Uri uri = Uri.fromParts("package",getActivity().getPackageName(), null);
            intent.setData(uri);
            usageAccessLauncher.launch(intent);
            handler.postDelayed(runnable, 1000);

        });

        accessibility_service_btn.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            accessibilityLauncher.launch(intent);
        });


        return view;
    }


    Handler handler = new Handler();
    Runnable runnable = () -> {
        if (isAdded() && getActivity() != null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                return;
            }
            Log.d("Grant Permission Fragment", "Checking Permissions");

            if(ManagePermissions.isUsagePermissionGranted(getActivity()) && ManagePermissions.isAccessibilityServiceEnabled(requireContext(), AppAccessibilityService.class)){
                Button continueBtn = requireView().findViewById(R.id.continue_btn);
                continueBtn.setEnabled(true);
                Log.d("Grant Permission Fragment","Enabled Button");
                handler.removeCallbacks(this.runnable);
            }else{
                handler.postDelayed(this.runnable, 1000);
            }
        }
    };
}
