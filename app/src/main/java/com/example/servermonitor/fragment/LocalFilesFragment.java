package com.example.servermonitor.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import android.widget.EditText;
import android.widget.Toast;

import com.example.servermonitor.MainActivity;
import com.example.servermonitor.R;
import com.example.servermonitor.adapter.LocalFilesAdapter;
import com.example.servermonitor.databinding.FragmentBrowseServerFilesBinding;
import com.example.servermonitor.databinding.FragmentLocalFilesBinding;
import com.example.servermonitor.helper.LocalFileOperations;

import java.io.File;
import java.io.IOException;


public class LocalFilesFragment extends Fragment {
    private FragmentLocalFilesBinding binding;
    private File[] files;
    private LocalFilesAdapter adapter;
    private File currentDirectory;
    private MainActivity activity;
    private Context context;
    public String currentPath;
    public String homePath;
    private File homeDirectory;

    public LocalFilesFragment() {
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
        binding = FragmentLocalFilesBinding.inflate(inflater, container, false);
        activity = (MainActivity) getActivity();
        activity.getSupportActionBar().setTitle("Local files");
        context = activity.getApplicationContext();
        setupListeners();
        setLoading();
        registerForContextMenu(binding.rvServerFiles);
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
        binding.btnDirectoryHome.setOnClickListener(v -> {
            goToPath(homeDirectory);
        });
        binding.btnRefreshDir.setOnClickListener(v -> {
            goToPath(currentDirectory);
        });
        binding.btnCreateFile.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(context).inflate(R.layout.single_string_dialog_layout, null);
            askStringInput(dialogView, "File name", (dialog, which) -> {
                EditText inputField = dialogView.findViewById(R.id.editText);
                String fileName = inputField.getText().toString();
                if (! LocalFileOperations.verifyFilename(fileName)) {
                    Toast.makeText(context, "Invalid filename", Toast.LENGTH_LONG).show();
                    return;
                }
                File newFile = new File(currentPath + "/" + fileName);
                try {
                    newFile.createNewFile();
                    goToPath(currentDirectory);
                } catch (IOException e) {
                    Toast.makeText(context, "Failed creating file.", Toast.LENGTH_LONG).show();
                }
            });
        });
        binding.btnCreateDirectory.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(context).inflate(R.layout.single_string_dialog_layout, null);
            askStringInput(dialogView, "Directory name", (dialog, which) -> {
                EditText inputField = dialogView.findViewById(R.id.editText);
                String directoryName = inputField.getText().toString();
                if (! LocalFileOperations.verifyFilename(directoryName)) {
                    Toast.makeText(context, "Invalid directory name", Toast.LENGTH_LONG).show();
                    return;
                }
                File newFile = new File(currentPath + "/" + directoryName);
                boolean success = newFile.mkdir();
                goToPath(currentDirectory);
                if (!success)
                    Toast.makeText(context, "Failed creating directory.", Toast.LENGTH_LONG).show();
            });
        });
    }

    private void askStringInput(View dialogView, String title, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView)
                .setTitle(title)
                .setPositiveButton("OK", listener)
                .setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
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
            case "Edit":
                break;
            case "Rename":
                break;
            case "Delete":
                new Thread(() -> {
                    Boolean deleted = false;
                    if (file.isDirectory())
                        deleted = LocalFileOperations.removeDirectory(file);
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
}
