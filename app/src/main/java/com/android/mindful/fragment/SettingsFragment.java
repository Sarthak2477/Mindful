package com.android.mindful.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.widget.TextView;

import com.android.mindful.R;
import com.android.mindful.activity.PrivacyPolicyActivity;


public class SettingsFragment extends Fragment {



    public SettingsFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2) {
      return  new SettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_settings, container, false);
        TextView txtPrivacyPolicy = view.findViewById(R.id.text_privacy_policy);
        txtPrivacyPolicy.setOnClickListener(v -> {
           startActivity(new Intent(getActivity(), PrivacyPolicyActivity.class));
        });
        return view;
    }
}