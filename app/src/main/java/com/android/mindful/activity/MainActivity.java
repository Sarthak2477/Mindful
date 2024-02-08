package com.android.mindful.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.mindful.service.AppAccessibilityService;
import com.android.mindful.fragment.EditFragment;
import com.android.mindful.R;
import com.android.mindful.fragment.SettingsFragment;
import com.android.mindful.fragment.StatsFragment;
import com.android.mindful.managers.ManagePermissions;
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
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        Log.d(TAG, "First Launch: " + preferences.getBoolean("first_launch", true));
        if(preferences.getBoolean("first_launch", true)){
//            Launch Setup Activity
            Log.d(TAG,"Launch Setup Activity");

            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("first_launch", false);
            editor.apply();
            editor.commit();
            Log.d(TAG, "First Launch: " + preferences.getBoolean("first_launch", true));

            startActivity(new Intent(MainActivity.this, SetupActivity.class));
        }else{
            if(!ManagePermissions.isUsagePermissionGranted(MainActivity.this) || !ManagePermissions.isAccessibilityServiceEnabled(MainActivity.this, AppAccessibilityService.class) && !preferences.getBoolean("first_launch", false)){
                Intent intent = new Intent(MainActivity.this, SetupActivity.class);
                intent.putExtra("for_permissions", true);
                startActivity(intent);
            }

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