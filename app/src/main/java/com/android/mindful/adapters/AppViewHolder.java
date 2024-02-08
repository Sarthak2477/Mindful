package com.android.mindful.adapters;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.mindful.R;

public class AppViewHolder extends RecyclerView.ViewHolder {
    ImageView appIcon;
    TextView appName;
    TextView stat;

    CheckBox box;
    public AppViewHolder(@NonNull View itemView) {
        super(itemView);
        appIcon = itemView.findViewById(R.id.appIcon);
        appName = itemView.findViewById(R.id.appName);
        stat = itemView.findViewById(R.id.stat);
        box = itemView.findViewById(R.id.app_checkbox);
    }
}
