package com.example.servermonitor.fragment;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
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
import android.widget.Toast;

import com.example.servermonitor.MainActivity;
import com.example.servermonitor.R;
import com.example.servermonitor.service.SshSessionWorker;
import com.example.servermonitor.adapter.ServerFilesAdapter;
import com.example.servermonitor.databinding.FragmentBrowseServerFilesBinding;
import com.example.servermonitor.helper.FileLoadingProgressMonitor;
import com.example.servermonitor.helper.FileOperations;
import com.example.servermonitor.helper.UiHelper;
import com.example.servermonitor.model.ServerModel;
import com.example.servermonitor.model.SshKeyModel;
import com.example.servermonitor.service.SshKeyService;
import com.example.servermonitor.service.SshShellSessionWorker;
import com.jcraft.jsch.ChannelSftp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;
import java.util.Vector;

public class BrowseServerFilesFragment extends Fragment {
    private static final int REQUEST_CODE_CREATE_DOCUMENT = 25;
    private static final int REQUEST_CODE_OPEN_DOCUMENT = 1;
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
    private ServerModel server;
    private ChannelSftp.LsEntry fileToSave;

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
        activity.getSupportActionBar().setTitle(R.string.fragment_browse_files_title);
        context = activity.getApplicationContext();
        setupListeners();
        sshKeyService = new SshKeyService(MainActivity.database);
        setLoading();
        registerForContextMenu(binding.rvServerFiles);
        new Thread(() -> {
            Bundle args = getArguments();
            server = args.getParcelable("serverModel");
            activity.getSupportActionBar().setTitle("Filesystem (" + server.getName() + ")");
            FileOperations.serverModelLastBrowsedFiles = server;
            Optional<SshKeyModel> sshKey = sshKeyService.getSshKeyForServer(server);
            try {
                sshSessionWorker = new SshSessionWorker(context, server, sshKey);
                shellSessionWorker = new SshShellSessionWorker(context, server, sshKey);
                String homeDir = sshSessionWorker.executeSingleCommand("pwd").replace("\n", "");
                currentPath = homeDir;
                homePath = homeDir;
                binding.tvPath.setText(homeDir);
                lsEntries = shellSessionWorker.listDir(homeDir, this);
                if (lsEntries == null) {
                    lsEntries = new Vector<>();
                    activity.runOnUiThread(() -> {
                        Toast.makeText(context, "Error occurred while loading directory listing", Toast.LENGTH_LONG).show();
                    });
                }
                adapter = new ServerFilesAdapter(context, lsEntries, activity, this);
                activity.runOnUiThread(() -> {
                    binding.rvServerFiles.setAdapter(adapter);
                    setLoaded();
                    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
                    binding.rvServerFiles.setLayoutManager(layoutManager);
                    binding.rvServerFiles.setItemAnimator(new DefaultItemAnimator());
                });
            } catch (Exception e) {
                activity.runOnUiThread(() -> {
                    Toast.makeText(context, "Couldn't establish the connection with the server", Toast.LENGTH_LONG);
                });
            }
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
        binding.btnLocalFiles.setOnClickListener(v -> {
            NavController controller = Navigation.findNavController(binding.getRoot());
            controller.navigate(R.id.localFilesFragment);
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
            if (lsEntries == null) {
                lsEntries = new Vector<>();
                activity.runOnUiThread(() -> {
                    Toast.makeText(context, "Error occurred while loading directory listing", Toast.LENGTH_LONG).show();
                });
            }
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
            case "Copy":
                FileOperations.setRemoteFileForCopy(server, currentPath + "/" + entry.getFilename(), entry.getFilename());
                break;
            case "Download":
                new Thread(() -> {
                    List<Object> results = shellSessionWorker.copyFromServer(currentPath + "/" + entry.getFilename(), entry.getFilename(), context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath());
                    if (!(boolean) results.get(0)) {
                        activity.runOnUiThread(() ->
                                Toast.makeText(context, "Download failed", Toast.LENGTH_LONG).show());
                    } else {
                        FileLoadingProgressMonitor monitor = (FileLoadingProgressMonitor) results.get(1);
                        UiHelper.monitorProgress(getContext(), activity, monitor, () -> null);
                    }
                }).start();
                break;
            case "Save as":
                fileToSave = entry;
                UiHelper.createFilePicker(this, "*/*", entry.getFilename());
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
    private void writeFile(Uri uri) {
        try {
            OutputStream outputStream = activity.getContentResolver().openOutputStream(uri);
            if (outputStream != null) {
                new Thread(() -> {
                    List<Object> results = shellSessionWorker.copyFromServerUsingStreams(currentPath + "/" + fileToSave.getFilename(), outputStream);
                    if (!(boolean) results.get(0)) {
                        activity.runOnUiThread(() ->
                                Toast.makeText(context, "Download failed", Toast.LENGTH_LONG).show());
                    } else {
                        FileLoadingProgressMonitor monitor = (FileLoadingProgressMonitor) results.get(1);
                        UiHelper.monitorProgress(getContext(), activity, monitor, () -> null);
                    }
                }).start();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CREATE_DOCUMENT && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                new Thread(() -> {
                    writeFile(uri);
                }).start();
            }
        }
        if (requestCode == REQUEST_CODE_OPEN_DOCUMENT && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                new Thread(() -> {
                }).start();
            }
        }
    }


    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.inflate(R.menu.file_browser_menu);
        MenuItem pasteItem = popupMenu.getMenu().getItem(2);
        if (FileOperations.existsFileToCopy()) {
            if (FileOperations.existsRemoteFileToCopy() &&
            !UiHelper.serverStillExists(activity, FileOperations.getServerModelWantToCopyFrom())) {
               pasteItem.setEnabled(false);
            } else {
                pasteItem.setTitle("Paste " + FileOperations.getShortFileNameToCopy());
            }
        } else {
            pasteItem.setEnabled(false);
        }

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
            else if (itemId == R.id.miPaste) {
                new Thread(() -> {
                    FileOperations.copyFileToCopy(activity, getContext(), server, currentPath, () -> {
                        goToPath(".");
                        return null;
                    });
                }).start();
            }
            return false;
        });
        popupMenu.show();
    }
}