package com.example.servermonitor.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
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
import com.example.servermonitor.adapter.AlertsAdapter;
import com.example.servermonitor.databinding.FragmentAlertsBinding;
import com.example.servermonitor.model.AlertModel;
import com.example.servermonitor.service.AlertService;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class AlertsFragment extends Fragment {
    private Context context;
    private MainActivity activity;
    private AlertsAdapter adapter;
    private AlertService alertService;
    private ArrayList<AlertModel> alerts;
    private FragmentAlertsBinding binding;
    public AlertsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAlertsBinding.inflate(inflater, container, false);
        alertService = new AlertService(MainActivity.database);
        activity = (MainActivity) getActivity();
        activity.getSupportActionBar().setTitle(R.string.fragment_alerts_title);
        context = activity.getApplicationContext();
        setupListeners();
        setupAdapter();
        registerForContextMenu(binding.rvAlerts);
        return binding.getRoot();
    }
    private void setupListeners() {
        binding.fabAddAlert.setOnClickListener(v -> {
            NavController controller = Navigation.findNavController(binding.getRoot());
            controller.navigate(R.id.action_alertsFragment_to_editAlertFragment);
        });
    }

    private void setupAdapter() {
        new Thread(() -> {
            alerts = alertService.getAllAlerts();
            Bundle args = getArguments();
            if (args != null) {
                if (args.getInt("success") == 1) {
                    AlertModel alert = args.getParcelable("alertModel");
                    addOrEditAlert(alert);
                }
                getArguments().clear();
            }
            adapter = new AlertsAdapter(activity, alerts);
            activity.runOnUiThread(() -> {
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
                binding.rvAlerts.setLayoutManager(layoutManager);
                binding.rvAlerts.setItemAnimator(new DefaultItemAnimator());
                binding.rvAlerts.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }
    public void addOrEditAlert(AlertModel alert) {
        if (alert.getId() == 0) {
            int id = (int) alertService.addAlert(alert);
            alert.setId(id);
            alerts.add(alert);
            activity.alerts.add(alert);
        } else {
            alertService.updateAlert(alert);
            int pos = alerts.indexOf(alerts.stream().filter(a -> a.getId() == alert.getId()).findFirst().get());
            alerts.set(pos, alert);
            activity.alerts.set(pos, alert);
        }
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int pposition = adapter.selectedItemPosition;

        switch (item.getTitle().toString()) {
            case "Edit":
                NavController controller = Navigation.findNavController(binding.getRoot());
                Bundle bundle = new Bundle();
                bundle.putInt("edit", 1);
                bundle.putParcelable("alertModel", alerts.get(pposition));
                controller.navigate(R.id.action_alertsFragment_to_editAlertFragment, bundle);
                break;
            case "Delete":
                new Thread(() -> {
                    alertService.deleteAlert(alerts.get(pposition));
                    alerts.remove(pposition);
                    activity.alerts.remove(pposition);
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
        inflater.inflate(R.menu.alert_context_menu, menu);
    }
}