package com.example.servermonitor.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.servermonitor.R;
import com.example.servermonitor.databinding.FragmentRunScriptBinding;
import com.example.servermonitor.model.ServerModel;

import java.util.ArrayList;

public class RunScriptFragment extends Fragment {
    private FragmentRunScriptBinding binding;

    public RunScriptFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRunScriptBinding.inflate(inflater, container, false);
        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    public void setupListeners() {
        binding.btnRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
    public void runScriptOnServers(ArrayList<ServerModel> servers) {

    }
}