package com.example.servermonitor.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.servermonitor.MainActivity;
import com.example.servermonitor.R;
import com.example.servermonitor.databinding.FragmentServerBinding;
import com.example.servermonitor.db.Converters;
import com.example.servermonitor.db.ServerDatabase;
import com.example.servermonitor.db.entity.MonitoringRecordEntity;
import com.example.servermonitor.db.entity.MonitoringSessionEntity;
import com.example.servermonitor.model.ServerModel;
import com.example.servermonitor.service.LineChartStyler;
import com.example.servermonitor.service.MonitoringRecordService;
import com.example.servermonitor.service.PieChartStyler;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServerFragment extends Fragment {
    private static int COLOR_LIGHT_YELLOW;
    private static int COLOR_GREEN;
    private static int CHART_REFRESH_INTERVAL = 5;
    private FragmentServerBinding binding;
    private MainActivity activity;
    private Context context;
    private ServerDatabase database;
    private ServerModel serverModel;
    private PieChartStyler pieChartStyler;
    private LineChartStyler lineChartStyler;

    public ServerFragment() {
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
        binding = FragmentServerBinding.inflate(inflater, container, false);
        activity = (MainActivity) getActivity();
        context = activity.getApplicationContext();
        COLOR_LIGHT_YELLOW = ContextCompat.getColor(context, R.color.light_yellow);
        COLOR_GREEN = ContextCompat.getColor(context, R.color.pale_green);
        return binding.getRoot();
    }
    private void getServerModel(Bundle args) {
        serverModel = args.getParcelable("serverModel");
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        database = MainActivity.database;
        pieChartStyler = new PieChartStyler("used", "total", COLOR_LIGHT_YELLOW, COLOR_GREEN);
        lineChartStyler = new LineChartStyler();
        getServerModel(getArguments());
        updateMonitoringUiComponents();
        setupOnClickListeners();
        monitorServer();
    }

    public void setupOnClickListeners() {
        binding.btnChangeMonitoringState.setOnClickListener(v -> {
            if (serverModel.getMonitoringSessionId() != -1) {
                binding.btnChangeMonitoringState.setText("Start monitoring session");
                saveMonitoringSessionToDb();
            } else {
                binding.btnChangeMonitoringState.setText("Stop monitoring session");
                startNewMonitoringSession();
            }
        });

        binding.btnOpenTerminal.setOnClickListener(v -> {
            TerminalFragment.serverModel = serverModel;
            NavController controller = Navigation.findNavController(binding.getRoot());
            controller.navigate(R.id.terminalFragment);
        });
    }
    public void updateMonitoringUiComponents() {
        if (serverModel.getMonitoringSessionId() == -1) {
            binding.btnChangeMonitoringState.setText("Start monitoring session");
        } else {
            binding.btnChangeMonitoringState.setText("Stop monitoring session");
        }
    }
    public void saveMonitoringSessionToDb() {
        new Thread(() -> {
            int sessionId = serverModel.getMonitoringSessionId();
            serverModel.setMonitoringSessionId(-1);
            MonitoringSessionEntity sessionEntity = database.getMonitoringSessionDao().getMonitoringSession(sessionId);
            sessionEntity.dateEnded = Converters.dateToTimestamp(Calendar.getInstance().getTime());
            database.getMonitoringSessionDao().updateMonitoringSession(sessionEntity);
        }).start();
    }
    public void startNewMonitoringSession() {
        new Thread(() -> {
            MonitoringSessionEntity sessionEntity = new MonitoringSessionEntity();
            Date currentTime = Calendar.getInstance().getTime();
            sessionEntity.name = "session "
                    + currentTime.getHours()
                    + ":" + currentTime.getMinutes()
                    + " " + currentTime.getDay()
                    + "." + currentTime.getMonth()
                    + "." + currentTime.getYear();
            sessionEntity.dateStarted = Converters.dateToTimestamp(currentTime);
            database.getMonitoringSessionDao().addMonitoringSession(sessionEntity);
            sessionEntity = database.getMonitoringSessionDao().getMonitoringSessionByStartTime(Converters.dateToTimestamp(currentTime));
            serverModel.setMonitoringSessionId(sessionEntity.id);
        }).start();
    }

    public void monitorServer() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(() -> {
            updatePieCharts();
            updateLineCharts();
        }, 0, CHART_REFRESH_INTERVAL, TimeUnit.SECONDS);
    }
    public void updatePieCharts() {
        float memoryUsed = serverModel.getMemoryUsedMb();
        float memoryTotal = serverModel.getMemoryTotalMb();
        float diskUsed = (float) serverModel.getDiskUsedMb();
        float diskTotal = (float) serverModel.getDiskTotalMb();
        float cpuUsed = (float) serverModel.getCpuUsagePercent();
        float cpuTotal = 100f;
        if (memoryTotal == 0 || cpuUsed == -1 || diskTotal == 0) {
            activity.runOnUiThread(() -> setPieChartsHidden());
        } else {
            activity.runOnUiThread(() -> {
                pieChartStyler.stylePieChart(binding.pcMemory, memoryUsed, memoryTotal);
                pieChartStyler.stylePieChart(binding.pcCpu, cpuUsed, cpuTotal);
                pieChartStyler.stylePieChart(binding.pcDisk, diskUsed, diskTotal);
                setPieChartsVisible();
            });
        }
    }
    public void updateLineCharts() {
        int sessionId = serverModel.getMonitoringSessionId();
        ArrayList<MonitoringRecordEntity> monitoringRecords = new ArrayList<>(database.getMonitoringRecordDao().getAllByMonitoringSessionId(sessionId));
        if (sessionId == -1 || monitoringRecords.size() == 0) {
            activity.runOnUiThread(() -> setLineChartsHidden());
        } else {
            activity.runOnUiThread(() -> {
                lineChartStyler.styleLineChart(binding.lcMemory, monitoringRecords, LineChartStyler.LineChartDataType.DATA_MEMORY);
                lineChartStyler.styleLineChart(binding.lcCpu, monitoringRecords, LineChartStyler.LineChartDataType.DATA_CPU);
                lineChartStyler.styleLineChart(binding.lcStorage, monitoringRecords, LineChartStyler.LineChartDataType.DATA_DISK);
                setLineChartsVisible();
            });
        }
    }

    public void setLineChartsVisible() {
        binding.lcMemory.invalidate();
        binding.lcCpu.invalidate();
        binding.lcStorage.invalidate();
        binding.lcMemory.setVisibility(View.VISIBLE);
        binding.lcCpu.setVisibility(View.VISIBLE);
        binding.lcStorage.setVisibility(View.VISIBLE);
    }
    public void setLineChartsHidden() {
        binding.lcMemory.setVisibility(View.GONE);
        binding.lcCpu.setVisibility(View.GONE);
        binding.lcStorage.setVisibility(View.GONE);
    }
    public void setPieChartsVisible() {
        binding.pcMemory.invalidate();
        binding.pcCpu.invalidate();
        binding.pcDisk.invalidate();
        binding.pcMemory.setVisibility(View.VISIBLE);
        binding.tvLabelMemoryNoData.setVisibility(View.GONE);
        binding.pcCpu.setVisibility(View.VISIBLE);
        binding.tvLabelCpuNoData.setVisibility(View.GONE);
        binding.pcDisk.setVisibility(View.VISIBLE);
        binding.tvLabelDiskNoData.setVisibility(View.GONE);
    }
    public void setPieChartsHidden() {
        binding.pcMemory.setVisibility(View.GONE);
        binding.tvLabelMemoryNoData.setVisibility(View.VISIBLE);
        binding.pcCpu.setVisibility(View.GONE);
        binding.tvLabelCpuNoData.setVisibility(View.VISIBLE);
        binding.pcDisk.setVisibility(View.GONE);
        binding.tvLabelDiskNoData.setVisibility(View.VISIBLE);
    }
}