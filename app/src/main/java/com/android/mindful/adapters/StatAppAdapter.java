package com.android.mindful.adapters;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.mindful.R;
import com.android.mindful.model.AppStats;
import com.android.mindful.utils.SharedPrefUtils;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class StatAppAdapter extends RecyclerView.Adapter<StatAppViewHolder> {
    List<AppStats> appStatsList;

    Context context;
    public StatAppAdapter( Context context,List<AppStats> appStatsList) {
        this.appStatsList = appStatsList;
        this.context = context;
    }

    @NonNull
    @Override
    public StatAppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stat_app, parent, false);
        return new StatAppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatAppViewHolder holder, int position) {
        SharedPrefUtils prefUtils = new SharedPrefUtils(context);
        String packageName = appStatsList.get(position).getPackageName();
        Drawable appIcon = appStatsList.get(position).getAppIcon();
        holder.appIcon.setImageDrawable(appIcon);
        // You may also need to set other views in the ViewHolder with corresponding data from appStatsList
        holder.appName.setText(appStatsList.get(position).getAppName());
        holder.dailyAvg.setText(appStatsList.get(position).getDailyAvg());

        String compareLastWeek = appStatsList.get(position).getCompareLastWeek();

        if(compareLastWeek.charAt(0) == '+'){
            holder.comparedLastWeek.setTextColor(Color.RED);
        }else{
            holder.comparedLastWeek.setTextColor(Color.GREEN);
        }
        holder.comparedLastWeek.setText(compareLastWeek);

        initBarChart(holder.barChart);

        holder.barChart.setData(new BarData(getBarDataSet(position)));
        holder.barChart.getBarData().setBarWidth(0.9f);
        holder.barChart.setClickable(false);
        holder.barChart.getBarData().setValueTextColors(Collections.singletonList(Color.WHITE));

        holder.setTime.setOnClickListener(v->{
            Dialog timerDialog = new Dialog(context);
            timerDialog.setContentView(R.layout.timer_dialog);

            NumberPicker hoursPicker = timerDialog.findViewById(R.id.hoursPicker);
            hoursPicker.setMaxValue(23);

            NumberPicker minutesPicker = timerDialog.findViewById(R.id.minutesPicker);
            minutesPicker.setMaxValue(59);

            timerDialog.setCancelable(false);
            HashMap<String, Long> appTimerList = prefUtils.getAppTimer();
            if(appTimerList.containsKey(packageName)){
                long millis = appTimerList.get(packageName);
                int hours = (int) (millis / (1000 * 60 * 60));
                int minutes = (int) ((millis % (1000 * 60 * 60)) / (1000 * 60));
                hoursPicker.setValue(hours);
                minutesPicker.setValue(minutes);

            }
            hoursPicker.animate();
            minutesPicker.animate();


            timerDialog.findViewById(R.id.cancel_timer).setOnClickListener(view->{
                timerDialog.dismiss();
            });

            timerDialog.findViewById(R.id.ok_set_timer).setOnClickListener(view->{
                long millis = ((hoursPicker.getValue() * 60L) + minutesPicker.getValue()) * 60 * 1000;

                prefUtils.setAppTimer(millis, appStatsList.get(position).getPackageName());
                timerDialog.dismiss();

            });

            timerDialog.show();
        });

        // Update bar chart if needed
    }

    @NonNull
    private BarDataSet getBarDataSet(int position) {
        BarDataSet barDataSet = appStatsList.get(position).getBarDataSet();
        barDataSet.setValueFormatter((value, entry, dataSetIndex, viewPortHandler) -> {
           if(value >= 0){
               long minutes = (long) value;

               if (minutes < 60) {
                   return String.format("%dm", minutes);
               } else {
                   long hours = minutes / 60;
                   long remainingMinutes = minutes % 60;
                   return String.format("%dh %02dm", hours, remainingMinutes);
               }

           }
           return "";
        });
        return barDataSet;
    }


    @Override
    public int getItemCount() {
        return appStatsList.size() ;
    }

    private void initBarChart(BarChart barChart){
        //hiding the grey background of the chart, default false if not set
        barChart.setDrawGridBackground(false);
        //remove the bar shadow, default false if not set
        barChart.setDrawBarShadow(false);
        //remove border of the chart, default false if not set
        barChart.setDrawBorders(false);

        barChart.setTouchEnabled(false);

        barChart.setHighlightPerTapEnabled(false);
        barChart.setHighlightPerDragEnabled(false);

        //remove the description label text located at the lower right corner
        Description description = new Description();
        description.setEnabled(false);
        barChart.setDescription(description);
        barChart.setBorderColor(Color.WHITE);
        barChart.setBackgroundColor(0xC4B6E8);


        //setting animation for y-axis, the bar will pop up from 0 to its value within the time we set
        barChart.animateY(2000);
        //setting animation for x-axis, the bar will pop up separately within the time we set
        barChart.animateX(2000);

        barChart.setPinchZoom(false);
        XAxis xAxis = barChart.getXAxis();
        //change the position of x-axis to the bottom
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //set the horizontal distance of the grid line
        xAxis.setGranularity(1f);
        //hiding the x-axis line, default true if not set
        xAxis.setDrawAxisLine(false);
        //hiding the vertical grid lines, default true if not set
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(generateWeekDays()));

//        xAxis.setDrawLabels(false);


        YAxis leftAxis = barChart.getAxisLeft();
        //hiding the left y-axis line, default true if not set
        leftAxis.setDrawAxisLine(false);
        leftAxis.setDrawGridLines(false);
        leftAxis.setDrawLabels(false);


        YAxis rightAxis = barChart.getAxisRight();
        //hiding the right y-axis line, default true if not set
        rightAxis.setDrawAxisLine(false);
        rightAxis.setDrawLabels(false);

        Legend legend = barChart.getLegend();
        //setting the shape of the legend form to line, default square shape
        legend.setEnabled(false);

    }

    private static String[] generateWeekDays() {
        String[] daysOfWeek = new String[]{"Sun", "Mon", "Tues", "Wed", "Thurs", "Fri", "Sat"};

        // Get the current day of the week
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        // Adjust the days of the week array based on the current day
        String[] adjustedWeekDays = new String[7];
        for (int i = 0; i < 7; i++) {
            adjustedWeekDays[i] = daysOfWeek[(dayOfWeek + i) % 7];
        }
        Log.d("StatAppAdapter", Arrays.toString(adjustedWeekDays));
        return adjustedWeekDays;
    }
}
