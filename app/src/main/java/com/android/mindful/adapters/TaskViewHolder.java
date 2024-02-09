package com.android.mindful.adapters;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.android.mindful.R;


public class TaskViewHolder extends AppViewHolder{

    CheckBox checkTask;

    TextView itemText;

    Button btnDelete;

    public TaskViewHolder(@NonNull View itemView) {
        super(itemView);
        checkTask = itemView.findViewById(R.id.check_task);
        itemText = itemView.findViewById(R.id.task_item_text);
        btnDelete = itemView.findViewById(R.id.task_delete);
    }
}
