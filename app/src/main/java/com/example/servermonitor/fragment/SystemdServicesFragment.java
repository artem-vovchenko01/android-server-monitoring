package com.example.servermonitor.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.servermonitor.MainActivity;
import com.example.servermonitor.adapter.SystemdServicesAdapter;
import com.example.servermonitor.databinding.FragmentSystemdServicesBinding;
import com.example.servermonitor.model.ServerModel;
import com.example.servermonitor.model.SystemdServiceModel;
import com.example.servermonitor.service.SshKeyService;
import com.example.servermonitor.service.SshSessionWorker;

import java.util.ArrayList;

public class SystemdServicesFragment extends Fragment {
    private static final String GET_SYSTEMD_SERVICES_COMMAND =
            "systemctl --no-pager --plain list-units --type=service | head -n -6 | tail -n +2";
    private FragmentSystemdServicesBinding binding;
    private MainActivity activity;
    private Context context;
    private ServerModel server;
    public SshSessionWorker sshSessionWorker;
    private SshKeyService sshKeyService;
    private ArrayList<SystemdServiceModel> systemdServices;
    private SystemdServicesAdapter adapter;

    public SystemdServicesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSystemdServicesBinding.inflate(inflater, container, false);
        activity = (MainActivity) getActivity();
        context = activity.getApplicationContext();
        activity.getSupportActionBar().setTitle("Systemd services");
        setupUiComponents();
        new Thread(() -> {
            Bundle args = getArguments();
            server = args.getParcelable("serverModel");
            activity.runOnUiThread(() -> {
                activity.getSupportActionBar().setTitle("Systemd services (" + server.getName() + ")");
            });
            sshKeyService = new SshKeyService(MainActivity.database);
            systemdServices = new ArrayList<>();
            try {
                sshSessionWorker = new SshSessionWorker(context, server, sshKeyService.getSshKeyForServer(server));
            } catch (Exception e) {
                displayError("Error occurred while establishing SSH connection");
                return;
            }
            String output = null;
            try {
                output = sshSessionWorker.executeSingleCommand(GET_SYSTEMD_SERVICES_COMMAND);
            } catch (Exception e) {
                displayError("Error occurred while trying to fetch data");
                return;
            }
            parseSystemdServices(output);
            adapter = new SystemdServicesAdapter(context, systemdServices, activity, this);
            activity.runOnUiThread(() -> {
                binding.rvSystemdServices.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            });
        }).start();
        return binding.getRoot();
    }
    public void displayError(String error) {
        activity.runOnUiThread(() -> {
            Toast.makeText(context, error, Toast.LENGTH_LONG).show();
        });
    }

    public void setupUiComponents() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        binding.rvSystemdServices.setLayoutManager(layoutManager);
        binding.rvSystemdServices.setItemAnimator(new DefaultItemAnimator());
    }
    private void parseSystemdServices(String systemctlOutput) {
        String[] lines = systemctlOutput.split("\n");
        for (String line : lines) {
            systemdServices.add(new SystemdServiceModel(line));
        }
    }
}