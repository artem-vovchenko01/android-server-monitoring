package com.example.servermonitor.fragment;

import android.content.Context;
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
import com.example.servermonitor.adapter.LocalFilesAdapter;
import com.example.servermonitor.databinding.FragmentLocalFilesBinding;
import com.example.servermonitor.helper.FileLoadingProgressMonitor;
import com.example.servermonitor.helper.FileOperations;
import com.example.servermonitor.helper.UiHelper;
import com.example.servermonitor.model.ServerModel;
import com.example.servermonitor.model.SshKeyModel;
import com.example.servermonitor.service.SshKeyService;
import com.example.servermonitor.service.SshShellSessionWorker;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class LocalFilesFragment extends Fragment {
    private FragmentLocalFilesBinding binding;
    private File[] files;
    private LocalFilesAdapter adapter;
    private File currentDirectory;
    private MainActivity activity;
    private SshKeyService sshKeyService;
    private Context context;
    public String currentPath;
    public String homePath;
    private File homeDirectory;

    public LocalFilesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (MainActivity) getActivity();
        activity.getSupportActionBar().setTitle(R.string.fragment_local_files_title);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLocalFilesBinding.inflate(inflater, container, false);
        activity = (MainActivity) getActivity();
        sshKeyService = new SshKeyService(MainActivity.database);
        context = activity.getApplicationContext();
        setupListeners();
        setLoading();
        registerForContextMenu(binding.rvServerFiles);
        if (FileOperations.serverModelLastBrowsedFiles == null || (! UiHelper.serverStillExists(activity, FileOperations.serverModelLastBrowsedFiles))) {
            if (activity.serverModels.size() > 0)
                FileOperations.serverModelLastBrowsedFiles = activity.serverModels.get(0);
            else
                binding.btnToServerFiles.setEnabled(false);
        }
        new Thread(() -> {
            String homeDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();
            currentPath = homeDir;
            homePath = homeDir;
            currentDirectory = new File(homeDir);
            homeDirectory = currentDirectory;
            files = currentDirectory.listFiles();
            adapter = new LocalFilesAdapter(context, files, currentDirectory, activity, this);
            activity.runOnUiThread(() -> {
                binding.tvPath.setText(homeDir);
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
        binding.btnDirectoryHome.setOnClickListener(v -> goToPath(homeDirectory));
        binding.btnRefreshDir.setOnClickListener(v -> goToPath(currentDirectory));
        binding.btnMenu.setOnClickListener(this::showPopupMenu);
        binding.btnToServerFiles.setOnClickListener(v -> {
            NavController controller = Navigation.findNavController(binding.getRoot());
            Bundle args = new Bundle();
            args.putParcelable("serverModel", FileOperations.serverModelLastBrowsedFiles);
            controller.navigate(R.id.browseServerFilesFragment, args);
        });
    }
    private void createDirectory() {
        UiHelper.createDirectoryAfterDialog(getContext(), directoryName -> {
            File newFile = new File(currentPath + "/" + directoryName);
            boolean success = newFile.mkdir();
            goToPath(currentDirectory);
            if (!success)
                Toast.makeText(context, "Failed creating directory.", Toast.LENGTH_LONG).show();
        });
    }
    private void createFile() {
        UiHelper.createFileAfterDialog(getContext(), fileName -> {
            File newFile = new File(currentPath + "/" + fileName);
            boolean success = true;
            try {
                success = newFile.createNewFile();
            } catch (IOException e) {
                success = false;
            }
            if (! success)
                Toast.makeText(context, "Failed creating file.", Toast.LENGTH_LONG).show();
            goToPath(currentDirectory);
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

    public void goToPath(File directory) {
        new Thread(() -> {
            setLoading();
            currentDirectory = directory;
            files = directory.listFiles();
            currentPath = directory.getAbsolutePath();
            activity.runOnUiThread(() -> {
                binding.tvPath.setText(currentPath);
            });
            adapter.setFiles(files, currentDirectory);
            activity.runOnUiThread(() -> {
                setLoaded();
                adapter.notifyDataSetChanged();
            });
        }).start();
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (adapter.selectedItemPosition != 0 && adapter.selectedItemPosition != 1) {
            MenuInflater inflater = activity.getMenuInflater();
            inflater.inflate(R.menu.local_file_context_menu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        int pposition = adapter.selectedItemPosition;
        File file = files[pposition - 2];
        switch (item.getTitle().toString()) {
            case "Copy":
                FileOperations.localFileToCopy = file;
                break;
            case "Edit":
                break;
            case "Rename":
                break;
            case "Delete":
                new Thread(() -> {
                    Boolean deleted = false;
                    if (file.isDirectory())
                        deleted = FileOperations.removeDirectory(file);
                    else
                        deleted = file.delete();
                    Boolean finalDeleted = deleted;
                    activity.runOnUiThread(() -> {
                        if (finalDeleted) {
                            goToPath(currentDirectory);
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
        MenuItem pasteItem = popupMenu.getMenu().getItem(2);
        if (FileOperations.remoteFileToCopy != null && UiHelper.serverStillExists(activity, FileOperations.serverModelWantToCopyFrom)) {
            pasteItem.setTitle("Paste " + FileOperations.remoteFileShortName);
        } else {
            pasteItem.setEnabled(false);
        }

        popupMenu.setOnMenuItemClickListener(i -> {
            int itemId = i.getItemId();
            if (itemId == R.id.miNewDirectory) {
                createDirectory();
            }
            else if (itemId ==R.id.miNewFile) {
                createFile();
            }
            else if (itemId == R.id.miPaste) {
                new Thread(() -> {
                    ServerModel server = FileOperations.serverModelWantToCopyFrom;
                    Optional<SshKeyModel> sshKey = sshKeyService.getSshKeyForServer(server);
                    SshShellSessionWorker shellSessionWorker = null;
                    try {
                        shellSessionWorker = new SshShellSessionWorker(context, FileOperations.serverModelWantToCopyFrom, sshKey);
                    } catch (Exception e) {
                        activity.runOnUiThread(() ->
                                Toast.makeText(context, "Error while connecting to server.", Toast.LENGTH_LONG));
                        return;
                    }
                    List<Object> results = shellSessionWorker.copyFromServer(FileOperations.remoteFileToCopy, FileOperations.remoteFileShortName, currentPath);
                    if ((boolean) results.get(0)) {
                        FileLoadingProgressMonitor monitor = (FileLoadingProgressMonitor) results.get(1);
                        UiHelper.monitorProgress(getContext(), activity, monitor, () -> {
                            goToPath(currentDirectory);
                            return null;
                        });
                    }
                }).start();
            }
            return false;
        });
        popupMenu.show();
    }
}
