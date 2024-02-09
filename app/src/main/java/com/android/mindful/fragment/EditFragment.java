package com.android.mindful.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.android.mindful.R;
import com.android.mindful.adapters.TaskViewHolder;
import com.android.mindful.adapters.TasksAdapter;
import com.android.mindful.model.Task;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class EditFragment extends Fragment {


    private final String TASK_LIST_KEY = "task_list";
    public EditFragment() {
        // Required empty public constructor
    }

    public static EditFragment newInstance() {
        EditFragment fragment = new EditFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit, container, false);
        Button addTask = view.findViewById(R.id.btn_add_task);
        EditText editText = view.findViewById(R.id.text_task);
        RecyclerView recyclerView = view.findViewById(R.id.task_recycler_view);

        List<Task> taskList = getList(getActivity(), TASK_LIST_KEY);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        TasksAdapter adapter = new TasksAdapter(taskList);
        recyclerView.setAdapter(adapter);


        addTask.setOnClickListener(v -> {
           if(taskList != null){
               taskList.add(new Task(editText.getText().toString(), false, System.currentTimeMillis()));
               saveList(getActivity(), TASK_LIST_KEY, taskList);
               adapter.taskList = taskList;
               adapter.notifyDataSetChanged();
               editText.setText("");

           }

        });
        return  view;
    }

    // Convert a list to a JSON string
    public static void saveList(Context context, String key, List<Task> list) {
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.apply();
    }


    // Retrieve a list from a JSON string
    public static List<Task> getList(Context context, String key) {
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(key, null);

        if (json != null) {
            Type type = new TypeToken<List<Task>>(){}.getType();
            List<Task> taskList = gson.fromJson(json, type);
            return taskList;
        } else {
            return new ArrayList<>();
        }
    }


}