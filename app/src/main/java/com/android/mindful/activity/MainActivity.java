package com.android.mindful.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.mindful.fragment.EditFragment;
import com.android.mindful.R;
import com.android.mindful.fragment.SettingsFragment;
import com.android.mindful.fragment.StatsFragment;
import com.android.mindful.managers.ManagePermissions;
import com.android.mindful.service.AppForegroundService;
import com.android.mindful.utils.SharedPrefUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = "MainActivity";

    private final Fragment statsFragment = new StatsFragment();
    private final Fragment editFragment = new EditFragment();
    private  final Fragment settingsFragment = new SettingsFragment();

    private final FragmentManager fragmentManager = getSupportFragmentManager();
    private Fragment activeFragment = statsFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        SharedPrefUtils prefUtils = new SharedPrefUtils(this);

        if(prefUtils.isFirstLaunch()){
//            Launch Setup Activity
            Log.d(TAG,"Launch Setup Activity");

            prefUtils.setFirstLaunch(false);
            startActivity(new Intent(MainActivity.this, SetupActivity.class));
        }else{
            if(!ManagePermissions.isUsagePermissionGranted(MainActivity.this) && !prefUtils.isFirstLaunch()){
                Intent intent = new Intent(MainActivity.this, SetupActivity.class);
                intent.putExtra("for_permissions", true);
                startActivity(intent);
            }

            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == Activity.RESULT_OK) {
                                System.out.println("Overlay   Permission Granted");

                            }
                        }).launch(intent);
            }

            Intent serviceIntent = new Intent(this, AppForegroundService.class);
            startService(serviceIntent);


            BottomNavigationView bottomNavView = findViewById(R.id.bottom_navigation);
            bottomNavView.setOnItemSelectedListener(navItemSelectedListener);

            // Load the home fragment by default
            fragmentManager.beginTransaction().replace(R.id.container, statsFragment).commit();
        }
    }

    private final NavigationBarView.OnItemSelectedListener navItemSelectedListener = menuItem -> {
        Fragment selectedFragment = null;

        if(menuItem.getItemId() == R.id.action_stats){
            selectedFragment = statsFragment;
        }else if(menuItem.getItemId() == R.id.action_edit){
            selectedFragment = editFragment;
        }else if(menuItem.getItemId() == R.id.action_settings){
            selectedFragment = settingsFragment;
        }
        if (selectedFragment != null && selectedFragment != activeFragment) {
            switchFragment(selectedFragment);
            activeFragment = selectedFragment;
        }
        return true;
    };
    private void switchFragment(Fragment selectedFragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);

        // Add the fragment
        transaction.replace(R.id.container, selectedFragment);

        // Commit the transaction
        transaction.commit();
    }
}