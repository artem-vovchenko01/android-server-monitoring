package com.example.servermonitor.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.example.servermonitor.MainActivity;
import com.example.servermonitor.R;
import com.example.servermonitor.databinding.FragmentEditAlertBinding;
import com.example.servermonitor.model.AlertModel;
import com.example.servermonitor.model.ServerModel;
import com.example.servermonitor.service.ServerService;

import java.util.ArrayList;

public class EditAlertFragment extends Fragment {
    private FragmentEditAlertBinding binding;
    private MainActivity activity;
    private ServerService serverService;
    private ArrayList<ServerModel> servers;
    private Context context;
    private AlertModel alertModel;

    public EditAlertFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEditAlertBinding.inflate(inflater, container, false);
        activity = (MainActivity) getActivity();
        activity.getSupportActionBar().setTitle(R.string.fragment_edit_alert_create_title);
        context = activity.getApplicationContext();
        serverService = new ServerService(MainActivity.database);
        binding.spinAlertType.setEnabled(false);
        binding.spinServer.setEnabled(false);
        setupAdapters();
        setupListeners();
        alertModel = new AlertModel();
        Bundle args = getArguments();
        fetchData(args);
        return binding.getRoot();
    }
    private void setupAdapters() {
        new Thread(() -> {
            ArrayAdapter<AlertModel.AlertType> alertTypeAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, AlertModel.AlertType.values());
            servers = serverService.getAllServers();
            ArrayAdapter<ServerModel> serverAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, servers);
            binding.spinAlertType.setAdapter(alertTypeAdapter);
            binding.spinServer.setAdapter(serverAdapter);
            activity.runOnUiThread(() -> {
                binding.spinAlertType.setEnabled(true);
                binding.spinServer.setEnabled(true);
            });
        }).start();
    }
    private void fetchData(Bundle args) {
        new Thread(() -> {
            activity.runOnUiThread(() -> {
                if (args != null) {
                    if (args.getInt("edit") == 1) {
                        fillDataOfExistingAlert(args.getParcelable("alertModel"));
                        activity.getSupportActionBar().setTitle(getString(R.string.fragment_edit_alert_edit_title) + " " + alertModel.getName());
                    }
                    args.clear();
                }
            });
        }).start();
    }
    private void fillDataOfExistingAlert(AlertModel alertModel) {
        this.alertModel = alertModel;
        binding.etAlertName.setText(alertModel.getName());
        binding.etThresholdValue.setText("" + alertModel.getThresholdValue());
        AlertModel.AlertType[] types = AlertModel.AlertType.values();
        int valuePos = 0;
        for (AlertModel.AlertType alertType : types) {
            if (alertModel.getAlertType() == alertType)
                break;
            valuePos++;
        }
        binding.spinAlertType.setSelection(valuePos);
        ServerModel server = servers.stream().filter(s -> s.getId() == alertModel.getServerId()).findFirst().get();
        binding.spinServer.setSelection(servers.indexOf(server));
    }
    private Bundle getResultBundle() {
        String alertName = binding.etAlertName.getText().toString();
        AlertModel.AlertType alertType = (AlertModel.AlertType) binding.spinAlertType.getSelectedItem();
        int thresholdValue = Integer.parseInt(binding.etThresholdValue.getText().toString());
        ServerModel server = (ServerModel) binding.spinServer.getSelectedItem();
        AlertModel alertModel = new AlertModel(this.alertModel.getId(), alertName, alertType, thresholdValue, server.getId());
        Bundle bundle = new Bundle();
        bundle.putParcelable("alertModel", alertModel);
        return bundle;
    }
    private void setupListeners() {
        binding.btnApply.setOnClickListener(v -> {
            NavController controller = Navigation.findNavController(binding.getRoot());
            Bundle args = getResultBundle();
            args.putInt("success", 1);
            controller.navigate(R.id.alertsFragment, args);
        });
        binding.btnCancel.setOnClickListener(v -> {
            NavController controller = Navigation.findNavController(binding.getRoot());
            Bundle args = new Bundle();
            args.putInt("success", 0);
            controller.navigate(R.id.alertsFragment, args);
        });
    }
}