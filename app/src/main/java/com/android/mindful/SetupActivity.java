package com.android.mindful;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class SetupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setup);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        Log.d("Setup", String.valueOf(Boolean.parseBoolean(getIntent().getStringExtra("for_permissions"))));
        Log.d("SetUp", String.valueOf(getIntent().getBooleanExtra("for_permissions", false)));
        if(getIntent().getBooleanExtra("for_permissions", false)){
            fragmentTransaction.replace(R.id.fragment_container, new GrantPermissionFragment());
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }else{
            fragmentTransaction.replace(R.id.fragment_container, new GetStartedFragment());
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }


    }
}