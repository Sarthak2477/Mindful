package com.android.mindful.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.mindful.R;
import com.android.mindful.adapters.TasksAdapter;
import com.android.mindful.model.Task;
import com.android.mindful.utils.SharedPrefUtils;

import org.w3c.dom.Text;

import java.util.List;

public class EditFragment extends Fragment {


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
        SharedPrefUtils prefUtils = new SharedPrefUtils(getActivity());

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit, container, false);
        Button addTask = view.findViewById(R.id.btn_add_task);
        EditText editText = view.findViewById(R.id.text_task);
        TextView noTaskMsg = view.findViewById(R.id.no_task_msg);
        EditText customMessage = view.findViewById(R.id.custom_message);
        Button addCustomMessage = view.findViewById(R.id.add_custom_msb);

        RecyclerView recyclerView = view.findViewById(R.id.task_recycler_view);

        List<Task> taskList = prefUtils.getTaskList();

        if(taskList.isEmpty()){
            noTaskMsg.setVisibility(View.VISIBLE);
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        TasksAdapter adapter = new TasksAdapter(getActivity(), taskList);
        recyclerView.setAdapter(adapter);


        addTask.setOnClickListener(v -> {
           if(taskList != null){
               noTaskMsg.setVisibility(View.GONE);
               taskList.add(new Task(editText.getText().toString(), false, System.currentTimeMillis()));
               prefUtils.saveTaskList(taskList);
               adapter.taskList = taskList;
               adapter.notifyDataSetChanged();
               editText.setText("");

           }

        });

        addCustomMessage.setOnClickListener(v->{
            String strCustomMessage = customMessage.getText().toString();
            if(!strCustomMessage.trim().isEmpty()) prefUtils.setCustomMessage(strCustomMessage);
            customMessage.setText("");
            customMessage.clearFocus();
        });
        return  view;
    }



}