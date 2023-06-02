package com.example.servermonitor.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.example.servermonitor.MainActivity;
import com.example.servermonitor.R;
import com.example.servermonitor.SshSessionWorker;
import com.example.servermonitor.adapter.ServerFilesAdapter;
import com.example.servermonitor.databinding.FragmentBrowseServerFilesBinding;
import com.example.servermonitor.model.ServerModel;
import com.example.servermonitor.model.SshKeyModel;
import com.example.servermonitor.service.SshKeyService;
import com.example.servermonitor.service.SshShellSessionWorker;
import com.jcraft.jsch.ChannelSftp;

import java.util.Optional;
import java.util.Vector;

public class BrowseServerFilesFragment extends Fragment {
    private FragmentBrowseServerFilesBinding binding;
    private Vector<ChannelSftp.LsEntry> lsEntries;
    private ServerFilesAdapter adapter;
    private MainActivity activity;
    private Context context;
    private SshShellSessionWorker shellSessionWorker;
    private SshSessionWorker sshSessionWorker;
    private SshKeyService sshKeyService;
    public String currentPath;
    private String homePath;

    public BrowseServerFilesFragment() {
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
        binding = FragmentBrowseServerFilesBinding.inflate(inflater, container, false);
        activity = (MainActivity) getActivity();
        activity.getSupportActionBar().setTitle("Filesystem");
        context = activity.getApplicationContext();
        setupListeners();
        sshKeyService = new SshKeyService(MainActivity.database);
        setLoading();
        new Thread(() -> {
            Bundle args = getArguments();
            ServerModel server = args.getParcelable("serverModel");
            Optional<SshKeyModel> sshKey = sshKeyService.getSshKeyForServer(server);
            try {
                sshSessionWorker = new SshSessionWorker(context, server, sshKey);
                shellSessionWorker = new SshShellSessionWorker(context, server, sshKey);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            String homeDir = sshSessionWorker.executeSingleCommand("pwd").replace("\n", "");
            currentPath = homeDir;
            homePath = homeDir;
            binding.tvPath.setText(homeDir);
            lsEntries = shellSessionWorker.listDir(homeDir, this);
            adapter = new ServerFilesAdapter(context, lsEntries, activity, this);
            activity.runOnUiThread(() -> {
                binding.rvServerFiles.setAdapter(adapter);
                setLoaded();
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
                binding.rvServerFiles.setLayoutManager(layoutManager);
                binding.rvServerFiles.setItemAnimator(new DefaultItemAnimator());
            });
        }).start();
        return binding.getRoot();
    }
    private void setupListeners() {
        binding.btnDirectoryRoot.setOnClickListener(v -> {
            goToPath("/");
        });
        binding.btnDirectoryHome.setOnClickListener(v -> {
            goToPath(homePath);
        });
    }

    private void setLoading() {
        activity.runOnUiThread(() -> {
            binding.tvLoading.setVisibility(View.VISIBLE);
            binding.rvServerFiles.setVisibility(View.GONE);
        });
    }
    private void setLoaded() {
        activity.runOnUiThread(() -> {
            binding.tvLoading.setVisibility(View.GONE);
            binding.rvServerFiles.setVisibility(View.VISIBLE);
        });
    }

    public void goToPath(String path) {
        new Thread(() -> {
            setLoading();
            lsEntries = shellSessionWorker.listDir(path, this);
            activity.runOnUiThread(() -> {
                binding.tvPath.setText(currentPath);
            });
            adapter.lsEntries = lsEntries;
            activity.runOnUiThread(() -> {
                setLoaded();
                adapter.notifyDataSetChanged();
            });
        }).start();
    }
}