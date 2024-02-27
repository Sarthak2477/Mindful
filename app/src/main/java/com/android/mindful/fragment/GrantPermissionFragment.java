package com.android.mindful.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
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

import com.android.mindful.R;
import com.android.mindful.activity.MainActivity;
import com.android.mindful.managers.ManagePermissions;

public class GrantPermissionFragment extends Fragment {


    private Button usage_access_btn, appear_on_top_btn;
    public GrantPermissionFragment(){

    }
    public static GrantPermissionFragment newInstance() {
        return new GrantPermissionFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_grant_permission, container, false);

        usage_access_btn = view.findViewById(R.id.usage_access_btn);
        appear_on_top_btn = view.findViewById((R.id.appear_on_top_btn));
        Button continue_btn = view.findViewById(R.id.continue_btn);
        continue_btn.setEnabled(false);

        continue_btn.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), MainActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });



        ActivityResultLauncher<Intent> appearOnTopResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        System.out.println("Overlay  Permission Granted");
                        appear_on_top_btn.setEnabled(false);
//                        appear_on_top_btn.setTextColor(Color.GRAY);
//                        appear_on_top_btn.setBackgroundColor(Color.GRAY);
                    }
                });

        ActivityResultLauncher<Intent> usageAccessResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        System.out.println("Usage Access Permission Granted");
                        usage_access_btn.setEnabled(false);
//                        usage_access_btn.setTextColor(Color.GRAY);
//                        usage_access_btn.setBackgroundColor(Color.GRAY);

                    }
                });

        handler.postDelayed(runnable, 1000);

        usage_access_btn.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            Uri uri = Uri.fromParts("package",getActivity().getPackageName(), null);
            intent.setData(uri);
            usageAccessResult.launch(intent);

        });

        appear_on_top_btn.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.fromParts("package", getActivity().getPackageName(), null));
            appearOnTopResult.launch(intent);
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

            if(ManagePermissions.isUsagePermissionGranted(getActivity())){
                usage_access_btn.setEnabled(false);
                usage_access_btn.setTextColor(Color.parseColor("#FD4A4646"));
                usage_access_btn.setBackgroundColor(Color.parseColor("#FD6C6B6B"));
            }


            if(Settings.canDrawOverlays(getActivity())){
                appear_on_top_btn.setEnabled(false);
                appear_on_top_btn.setTextColor(Color.parseColor("#FD4A4646"));
                appear_on_top_btn.setBackgroundColor(Color.parseColor("#FD6C6B6B"));
            }

            if(ManagePermissions.isUsagePermissionGranted(getActivity()) && Settings.canDrawOverlays(getActivity())){
                Button continueBtn = requireView().findViewById(R.id.continue_btn);
                continueBtn.setEnabled(true);
                continueBtn.setTextColor(Color.WHITE);
            }


            handler.postDelayed( this.runnable, 1000);
        }
    };
}
