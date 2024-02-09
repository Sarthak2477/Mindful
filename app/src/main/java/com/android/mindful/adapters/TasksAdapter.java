package com.android.mindful.adapters;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.mindful.R;
import com.android.mindful.model.Task;

import java.util.ArrayList;
import java.util.List;

public class TasksAdapter extends RecyclerView.Adapter<TaskViewHolder> {

    public List<Task> taskList = new ArrayList<>();

    public TasksAdapter(List<Task> taskList) {
        this.taskList = taskList;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
//        long createdAt = taskList.get(position).getCreatedAt();

        holder.itemText.setText(taskList.get(position).getTaskText());
        holder.checkTask.setChecked(taskList.get(position).isCompleted());
        holder.checkTask.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                taskList.get(position).setCompleted(true);
                holder.itemText.setPaintFlags(holder.itemText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                taskList.get(position).setCompleted(false);
                holder.itemText.setPaintFlags(holder.itemText.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }
        });
        holder.btnDelete.setOnClickListener(v -> {
            if(!taskList.isEmpty()){
                taskList.remove(position);
                notifyItemRemoved(position);
            }
        });

    }
    public int getItemCount() {
        if(taskList != null){
            return taskList.size();
        }else{
            return 0;
        }
    }
}
