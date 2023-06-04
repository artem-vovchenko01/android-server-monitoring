package com.example.servermonitor.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.servermonitor.MainActivity;
import com.example.servermonitor.R;
import com.example.servermonitor.SshSessionWorker;
import com.example.servermonitor.adapter.ServerFilesAdapter;
import com.example.servermonitor.databinding.FragmentBrowseServerFilesBinding;
import com.example.servermonitor.helper.FileLoadingProgressMonitor;
import com.example.servermonitor.helper.ServerFileOperations;
import com.example.servermonitor.helper.UiHelper;
import com.example.servermonitor.model.ServerModel;
import com.example.servermonitor.model.SshKeyModel;
import com.example.servermonitor.service.SshKeyService;
import com.example.servermonitor.service.SshShellSessionWorker;
import com.jcraft.jsch.ChannelSftp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
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
        registerForContextMenu(binding.rvServerFiles);
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
        binding.btnRefreshDir.setOnClickListener(v -> {
            goToPath(".");
        });
        binding.btnMenu.setOnClickListener(this::showPopupMenu);
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

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = activity.getMenuInflater();
        inflater.inflate(R.menu.file_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        int pposition = adapter.selectedItemPosition;
        ChannelSftp.LsEntry entry = lsEntries.get(pposition);
        switch (item.getTitle().toString()) {
            case "Edit":
                break;
            case "Download":
                new Thread(() -> {
                    List<Object> results = shellSessionWorker.downloadFile(entry.getFilename());
                    InputStream inputStream = (InputStream) results.get(0);
                    FileLoadingProgressMonitor monitor = (FileLoadingProgressMonitor) results.get(1);
                    if (inputStream != null) {
                        new Thread(() -> {
                            try {
                                ServerFileOperations.saveFileToLocalStorage(context, inputStream, entry.getFilename());
                                activity.runOnUiThread(() -> {
                                    Toast.makeText(context, "Downlaod succeeded", Toast.LENGTH_LONG).show();
                                });
                            } catch (IOException e) {
                                activity.runOnUiThread(() -> {
                                    Toast.makeText(context, "Downlaod failed", Toast.LENGTH_LONG).show();
                                });
                            }
                        }).start();
                    }
                }).start();
                break;
            case "Delete":
                new Thread(() -> {
                    Boolean deleted = shellSessionWorker.sftpRm(entry.getFilename());
                    activity.runOnUiThread(() -> {
                        if (deleted) {
                            goToPath(".");
                        } else {
                            Toast.makeText(context, "Delete operation failed", Toast.LENGTH_LONG).show();
                        }
                    });
                }).start();
                break;
        }
        return super.onContextItemSelected(item);
    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.inflate(R.menu.file_browser_menu);

        popupMenu.setOnMenuItemClickListener(i -> {
            int itemId = i.getItemId();
            if (itemId == R.id.miNewDirectory) {
                UiHelper.createDirectoryAfterDialog(getContext(), directoryName -> new Thread(() -> {
                    if (!shellSessionWorker.mkdir(directoryName)) {
                        activity.runOnUiThread(() -> Toast.makeText(context, "Couldn't create a directory", Toast.LENGTH_LONG).show());
                    }
                    goToPath(".");
                }).start());
            }
            else if (itemId ==R.id.miNewFile) {
                UiHelper.createFileAfterDialog(getContext(), fileName -> new Thread(() -> {
                    if(!shellSessionWorker.touch(fileName)) {
                        activity.runOnUiThread(() -> Toast.makeText(context, "Couldn't create a file", Toast.LENGTH_LONG).show());
                    }
                    goToPath(".");
                }).start());
            }
            else if (itemId == R.id.miPaste)
                return true;
            return false;
        });
        popupMenu.show();
    }
}