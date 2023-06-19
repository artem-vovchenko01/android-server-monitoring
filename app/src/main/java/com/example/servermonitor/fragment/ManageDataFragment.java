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
import android.widget.Toast;

import com.example.servermonitor.MainActivity;
import com.example.servermonitor.databinding.FragmentManageDataBinding;
import com.example.servermonitor.helper.UiHelper;
import com.example.servermonitor.model.ServerModel;
import com.example.servermonitor.service.DatabaseExporter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

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
                activity.runOnUiThread(() -> {
                    Toast.makeText(context, "Database successfully cleared", Toast.LENGTH_LONG).show();
                });
            }).start();
        });
        binding.importDb.setOnClickListener((v) -> {
            UiHelper.openFilePicker(this);
        });
        binding.exportDb.setOnClickListener((v) -> {
            Date currentTime = Calendar.getInstance().getTime();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
            String fileName = "serverMonitorExport_" + formatter.format(currentTime);
            UiHelper.createFilePicker(this, "application/json", fileName);
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
                    activity.runOnUiThread(() -> {
                        Toast.makeText(context, "Data successfully exported", Toast.LENGTH_LONG).show();
                    });
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
                    int serversCount = activity.serverModels.size();
                    activity.runOnUiThread(() -> {
                        Toast.makeText(context, "Successfully imported data, including " + serversCount + " servers", Toast.LENGTH_LONG).show();
                    });
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
}
