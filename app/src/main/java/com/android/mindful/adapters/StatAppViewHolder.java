package com.android.mindful.adapters;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.mindful.R;
import com.github.mikephil.charting.charts.BarChart;

public class StatAppViewHolder extends RecyclerView.ViewHolder {
    ImageView appIcon;
    TextView appName, dailyAvg, comparedLastWeek;
    BarChart barChart;

    Button setTime;


    public StatAppViewHolder(@NonNull View itemView) {
        super(itemView);
        appIcon = itemView.findViewById(R.id.app_stat_icon);
        appName = itemView.findViewById(R.id.app_stat_name);
        dailyAvg = itemView.findViewById(R.id.daily_avg);
        comparedLastWeek = itemView.findViewById(R.id.cmprd_last_week);
        barChart = itemView.findViewById(R.id.barChart);
        setTime = itemView.findViewById(R.id.btn_set_app_time);

    }
}
