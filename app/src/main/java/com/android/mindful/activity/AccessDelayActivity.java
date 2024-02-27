package com.android.mindful.activity;

import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.mindful.R;
import com.android.mindful.adapters.TasksAdapter;
import com.android.mindful.managers.ManageAppStats;
import com.android.mindful.model.Task;
import com.android.mindful.utils.SharedPrefUtils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccessDelayActivity extends AppCompatActivity {

    private ProgressBar delayProgressBar;
    SharedPrefUtils prefUtils;

    public String TAG = "Access Delay Activity";

    private TextView  textCustomMessage;

    public RecyclerView recyclerView;

    private Button continueBtn, addTask;

    public String packageName;

    public static boolean active;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access_delay);
//      Move activity to front on top of current app
        moveToFront();
        packageName = getIntent().getStringExtra("app_package");

        Log.d(TAG, "Activity active: " + active + " on " + packageName);



        prefUtils = new SharedPrefUtils(this);

        delayProgressBar = findViewById(R.id.delay_progress_bar);
        recyclerView = findViewById(R.id.delay_screen_recycler_view);
        addTask = findViewById(R.id.delay_screen_add_task);
        textCustomMessage = findViewById(R.id.delay_screen_custom_message);
        textCustomMessage.setText(prefUtils.getCustomMessage());

//       Get task List
        List<Task> taskList = prefUtils.getTaskList();


        if(!taskList.isEmpty()){

            Log.d(TAG, "Task List: " + taskList);

            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(new TasksAdapter(this,taskList));
            recyclerView.setVisibility(View.VISIBLE);
            findViewById(R.id.no_task_at_delay_screen).setVisibility(View.GONE );

        }else{
            findViewById(R.id.no_task_at_delay_screen).setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);

        }




        // Get the current time in milliseconds
        Calendar calendar = Calendar.getInstance();
        long endMillis = calendar.getTimeInMillis();

        // Set the time to the beginning of the day (midnight)
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long startMillis = calendar.getTimeInMillis();


        long totalTimeUsageInMillis = ManageAppStats.getUsageStatistics(this, packageName, startMillis, endMillis).timeInForeground;
        //
        long delayTime = calculateDelayTime((int) totalTimeUsageInMillis);
        Log.d(TAG, "Delay Time: " + delayTime);
        new CountDownTimer(delayTime, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Calculate the progress and update the ProgressBar
                int progress = (int) ((delayTime - millisUntilFinished) * 100 / delayTime);
                delayProgressBar.setProgress(progress);
            }

            @Override
            public void onFinish() {
                Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
                fadeIn.setDuration(500); // Adjust the duration of the fade-in animation as needed

                continueBtn.setVisibility(View.VISIBLE);
                continueBtn.startAnimation(fadeIn);
            }
        }.start();



        addTask.setOnClickListener(v->{
            active = false;
            Intent intent = new Intent(AccessDelayActivity.this, MainActivity.class);
            startActivity(intent);
        });

        continueBtn = findViewById(R.id.continue_btn);
        continueBtn.setOnClickListener(v -> {
            finishAndRemoveTask();
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity( launchIntent );

        });


        Button closeBtn = findViewById(R.id.close_btn);
        closeBtn.setOnClickListener(v -> {
                finishAndRemoveTask();
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        if(active)
            startActivity(getIntent());
        else
            active = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(active)
            finishAndRemoveTask();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    public static long calculateDelayTime(int usageTime) {
        long delayTime;

        if (usageTime < 600000) {  // Less than 10 minutes
            delayTime = 10000 + (long) (5000 * (usageTime / 600000));  // Linear interpolation between 10s to 15s
        } else if (usageTime < 3600000) {  // Less than 1 hour
            delayTime = 30000;  // 30s constant delay
        } else if (usageTime < 21600000) {  // Less than 6 hours
            delayTime = (long) 30000;  // Linear interpolation between 30s to 45s
        } else {
            delayTime = 60000;  // 60s constant delay
        }

        return delayTime;
    }
    @Override
    public void onBackPressed() {
        // Override the back button press to do nothing (disable going back)
    }



    protected void moveToFront() {
        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningTaskInfo> recentTasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

        for (int i = 0; i < recentTasks.size(); i++)
        {
            if(recentTasks.get(i).baseActivity != null){
                Log.d("Executed app", "Application executed : "
                        +recentTasks.get(i).baseActivity.toShortString()
                        + "\t\t ID: "+recentTasks.get(i).id);
                // bring to front
                if (recentTasks.get(i).baseActivity.toShortString().contains("com.android.mindful/com.android.mindful.activity.AccessDelayActivity")) {
                    Log.d("Access Delay","Moving To front");
                    activityManager.moveTaskToFront(recentTasks.get(i).id, ActivityManager.MOVE_TASK_WITH_HOME);
                }
            }
        }
    }


}