package com.example.servermonitor.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.servermonitor.MainActivity;
import com.example.servermonitor.databinding.FragmentMonitoringSessionBinding;
import com.example.servermonitor.db.ServerDatabase;
import com.example.servermonitor.db.entity.MonitoringRecordEntity;
import com.example.servermonitor.model.MonitoringSessionModel;
import com.example.servermonitor.service.LineChartStyler;

import java.util.ArrayList;

public class MonitoringSessionFragment extends Fragment {
    private FragmentMonitoringSessionBinding binding;
    private MainActivity activity;
    private Context context;
    private ServerDatabase database;
    private MonitoringSessionModel monitoringSession;
    private LineChartStyler lineChartStyler;

    public MonitoringSessionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMonitoringSessionBinding.inflate(inflater, container, false);
        activity = (MainActivity) getActivity();
        activity.getSupportActionBar().setTitle("Monitoring session");
        context = activity.getApplicationContext();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        database = MainActivity.database;
        lineChartStyler = new LineChartStyler();
        getMonitoringSessionModel(getArguments());
        setupOnClickListeners();
        new Thread(() -> {
            updateLineCharts();
        }).start();
    }

    public void getMonitoringSessionModel(Bundle args) {
        monitoringSession = args.getParcelable("monitoringSessionModel");
        args.clear();
    }

    public void setupOnClickListeners() {
    }
    public void updateLineCharts() {
        ArrayList<MonitoringRecordEntity> monitoringRecords = new ArrayList<>(database.getMonitoringRecordDao().getAllByMonitoringSessionId(monitoringSession.getId()));
        if (monitoringRecords.size() > 0) {
            activity.runOnUiThread(() -> {
                lineChartStyler.styleLineChart(binding.lcMemory, monitoringRecords, LineChartStyler.LineChartDataType.DATA_MEMORY, monitoringSession);
                lineChartStyler.styleLineChart(binding.lcCpu, monitoringRecords, LineChartStyler.LineChartDataType.DATA_CPU, monitoringSession);
                lineChartStyler.styleLineChart(binding.lcStorage, monitoringRecords, LineChartStyler.LineChartDataType.DATA_DISK, monitoringSession);
                binding.lcCpu.invalidate();
                binding.lcMemory.invalidate();
                binding.lcStorage.invalidate();
            });
        }
    }
}
