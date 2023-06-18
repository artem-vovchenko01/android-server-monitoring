package com.example.servermonitor.fragment;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.servermonitor.MainActivity;
import com.example.servermonitor.databinding.FragmentManageDataBinding;
import com.example.servermonitor.model.ServerModel;
import com.example.servermonitor.service.DatabaseExporter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class ManageDataFragment extends Fragment {
    private static final int REQUEST_CODE_CREATE_DOCUMENT = 25;
    private static final int REQUEST_CODE_OPEN_DOCUMENT = 1;
    private FragmentManageDataBinding binding;
    private MainActivity activity;
    private Context context;
    private DatabaseExporter databaseExporter;

    public ManageDataFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    public void setupListeners() {
        binding.clearDb.setOnClickListener((v) -> {
            new Thread(() -> {
                databaseExporter.clearDatabase();
                clearDataOnMainActivity();
            }).start();
        });
        binding.importDb.setOnClickListener((v) -> {
            openFilePicker();
        });
        binding.exportDb.setOnClickListener((v) -> {
            createFilePicker();
        });
    }
    public void clearDataOnMainActivity() {
        for (ServerModel server : activity.serverModels) {
            activity.stopJobForServer(server);
        }
        activity.serverModels.clear();
        activity.alerts.clear();
        activity.serverFragment = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentManageDataBinding.inflate(inflater, container, false);
        activity = (MainActivity) getActivity();
        context = activity.getApplicationContext();
        databaseExporter = new DatabaseExporter(MainActivity.database);
        setupListeners();
        return binding.getRoot();
    }

    private String readFile(Uri uri) {
        try {
            InputStream inputStream = activity.getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                    stringBuilder.append('\n');
                }
                inputStream.close();
                reader.close();

                return stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "";
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
                    String resultContent = readFile(uri);
                    clearDataOnMainActivity();
                    databaseExporter.importDatabaseData(resultContent);
                    activity.initializeData();
                }).start();
            }
        }
    }

    private void writeFile(Uri uri) {
        try {
            OutputStream outputStream = activity.getContentResolver().openOutputStream(uri);
            if (outputStream != null) {
                String content = databaseExporter.exportDatabaseData();
                outputStream.write(content.getBytes());
                outputStream.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createFilePicker() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, "serverMonitorExport.json");

        startActivityForResult(intent, REQUEST_CODE_CREATE_DOCUMENT);
    }
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");

        startActivityForResult(intent, REQUEST_CODE_OPEN_DOCUMENT);
    }
}
