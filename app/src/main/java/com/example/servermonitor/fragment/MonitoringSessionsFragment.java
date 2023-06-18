package com.example.servermonitor.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.servermonitor.MainActivity;
import com.example.servermonitor.R;
import com.example.servermonitor.adapter.MonitoringSessionsAdapter;
import com.example.servermonitor.databinding.FragmentMonitoringSessionsBinding;
import com.example.servermonitor.model.MonitoringSessionModel;
import com.example.servermonitor.model.ServerModel;
import com.example.servermonitor.service.MonitoringSessionService;

import java.util.ArrayList;

public class MonitoringSessionsFragment extends Fragment {
    private FragmentMonitoringSessionsBinding binding;
    private MonitoringSessionsAdapter adapter;
    private MainActivity activity;
    private Context context;
    private ServerModel server;
    private MonitoringSessionService monitoringSessionService;
    private ArrayList<MonitoringSessionModel> monitoringSessions;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (MainActivity) getActivity();
        activity.getSupportActionBar().setTitle(R.string.fragment_ssh_keys_title);
    }

    public MonitoringSessionsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMonitoringSessionsBinding.inflate(inflater, container, false);
        activity = (MainActivity) getActivity();
        context = activity.getApplicationContext();
        monitoringSessionService = new MonitoringSessionService(MainActivity.database);
        setupUiComponents();
        setupOnClickListeners();
        new Thread(() -> {
            Bundle args = getArguments();
            if (args != null) {
                server = args.getParcelable("serverModel");
            }
            monitoringSessions = monitoringSessionService.getMonitoringSessionsByServerId(server.getId());
            adapter = new MonitoringSessionsAdapter(context, monitoringSessions, activity);
            binding.rvMonitoringSessions.setAdapter(adapter);
        }).start();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        registerForContextMenu(binding.rvMonitoringSessions);
    }

    public void setupUiComponents() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        binding.rvMonitoringSessions.setLayoutManager(layoutManager);
        binding.rvMonitoringSessions.setItemAnimator(new DefaultItemAnimator());
    }

    private void setupOnClickListeners() {

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int pposition = adapter.selectedItemPosition;

        switch (item.getTitle().toString()) {
            case "Delete":
                new Thread(() -> {
                    monitoringSessionService.deleteMonitoringSession(monitoringSessions.get(pposition));
                    monitoringSessions.remove(pposition);
                    activity.runOnUiThread(() -> adapter.notifyDataSetChanged());
                }).start();
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = activity.getMenuInflater();
        inflater.inflate(R.menu.monitoring_session_context_menu, menu);
    }
}
