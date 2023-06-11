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
import android.widget.Button;

import com.example.servermonitor.MainActivity;
import com.example.servermonitor.R;
import com.example.servermonitor.service.SshSessionWorker;
import com.example.servermonitor.adapter.RunScriptAdapter;
import com.example.servermonitor.databinding.FragmentRunScriptBinding;
import com.example.servermonitor.dialog.ShowScriptOutputDialog;
import com.example.servermonitor.model.ServerModel;
import com.example.servermonitor.model.ShellScriptModel;
import com.example.servermonitor.model.SshKeyModel;
import com.example.servermonitor.service.ServerService;
import com.example.servermonitor.service.SshKeyService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class RunScriptFragment extends Fragment {
    private FragmentRunScriptBinding binding;
    private RunScriptAdapter adapter;
    private ShellScriptModel shellScript;
    private SshSessionWorker sshSessionWorker;
    private MainActivity activity;
    private Context context;
    private SshKeyService sshKeyService;
    private ServerService serverService;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (MainActivity) getActivity();
        activity.getSupportActionBar().setTitle(R.string.fragment_run_shell_script_title);
    }
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
        activity = (MainActivity) getActivity();
        context = activity.getApplicationContext();
        sshKeyService = new SshKeyService(MainActivity.database);
        Bundle args = getArguments();
        // Inflate the layout for this fragment
        shellScript = args.getParcelable("shellScriptModel");
        activity.getSupportActionBar().setTitle(getString(R.string.fragment_run_shell_script_title) + " " + shellScript.getName());
        setupListeners();
        serverService = new ServerService(MainActivity.database);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        binding.rvServers.setLayoutManager(layoutManager);
        binding.rvServers.setItemAnimator(new DefaultItemAnimator());
        new Thread(() -> {
            ArrayList<ServerModel> servers = serverService.getAllServers();
            adapter = new RunScriptAdapter(context, servers, activity);
            activity.runOnUiThread(() -> {
                binding.rvServers.setAdapter(adapter);
                binding.btnRun.setEnabled(true);
            });
        }).start();
        return binding.getRoot();
    }

    public void setupListeners() {
        binding.btnRun.setOnClickListener(v -> {
            HashMap<Integer, ServerModel> servers = adapter.chosenServers;
            for (HashMap.Entry<Integer, ServerModel> item : servers.entrySet()) {
                int position = item.getKey();
                View viewItem = binding.rvServers.getChildAt(position);
                Button btnScriptRunning = viewItem.findViewById(R.id.btnShowScriptRunning);
                Button btnShowOutput = viewItem.findViewById(R.id.btnShowOutput);
                btnScriptRunning.setVisibility(View.VISIBLE);
                new Thread(() -> {
                    ServerModel server = item.getValue();
                    Optional<SshKeyModel> sshKey = sshKeyService.getSshKeyForServer(server);
                    try {
                        sshSessionWorker = new SshSessionWorker(context, server, sshKey);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    String output = sshSessionWorker.executeShellScript(shellScript.getScriptData());
                    activity.runOnUiThread(() -> {
                       btnScriptRunning.setVisibility(View.GONE);
                       btnShowOutput.setOnClickListener((view) -> {
                           ShowScriptOutputDialog dialog = new ShowScriptOutputDialog();
                           dialog.setOutputText(output);
                           dialog.show(getChildFragmentManager(), "Script output");
                       });
                       btnShowOutput.setVisibility(View.VISIBLE);
                    });
                }).start();
            }
        });
    }
}