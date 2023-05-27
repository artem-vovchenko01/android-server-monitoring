package com.example.servermonitor.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.servermonitor.MainActivity;
import com.example.servermonitor.R;
import com.example.servermonitor.adapter.ServerAdapter;
import com.example.servermonitor.databinding.FragmentServersBinding;

public class ServersFragment extends Fragment {
    private FragmentServersBinding binding;
    private MainActivity activity;
    private Context context;
    private ServerAdapter serverAdapter;

    public ServersFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentServersBinding.inflate(inflater, container, false);
        activity = (MainActivity) getActivity();
        context = activity.getApplicationContext();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupUiComponents();
        setupOnClickListeners();
        serverAdapter = new ServerAdapter(context, activity.serverModels, activity);
        activity.serverAdapter = serverAdapter;
        binding.rvServers.setAdapter(serverAdapter);
    }

    public void setupUiComponents() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        binding.rvServers.setLayoutManager(layoutManager);
        binding.rvServers.setItemAnimator(new DefaultItemAnimator());
    }

    public void setupOnClickListeners() {
        //binding.fabAddServer.setOnClickListener(v -> showDialog());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        activity.serverAdapter = null;
    }
}