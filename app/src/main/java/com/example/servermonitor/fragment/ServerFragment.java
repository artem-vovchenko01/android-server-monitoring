package com.example.servermonitor.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.servermonitor.MainActivity;
import com.example.servermonitor.R;
import com.example.servermonitor.activity.TerminalActivity;
import com.example.servermonitor.adapter.ServerAdapter;
import com.example.servermonitor.databinding.FragmentServerBinding;
import com.example.servermonitor.db.Converters;
import com.example.servermonitor.db.ServerDatabase;
import com.example.servermonitor.db.entity.MonitoringRecordEntity;
import com.example.servermonitor.db.entity.MonitoringSessionEntity;
import com.example.servermonitor.model.ServerModel;
import com.example.servermonitor.service.MonitoringRecordService;
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
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        database = MainActivity.database;
        setupOnClickListeners();
        //requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
         //   @Override
          //  public void handleOnBackPressed() {
           //     NavController navController = Navigation.findNavController(activity, R.id.fragmentContainerView);
            //    navController.navigate(R.id.serversFragment);
           // }
        //});
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
            Intent intent = new Intent(context, TerminalActivity.class);
            TerminalActivity.serverModel = serverModel;
            startActivity(intent);
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
    public void updateLineCharts() {
        int sessionId = serverModel.getMonitoringSessionId();
        List<Entry> memoryEntries = null;
        List<Entry> cpuEntries = null;
        List<Entry> diskEntries = null;
        ArrayList<MonitoringRecordEntity> monitoringRecords = null;
        if (sessionId != -1) {
            monitoringRecords = MonitoringRecordService.getMonitoringRecordsByMonitoringSessionId(database, sessionId);
            if (monitoringRecords.size() > 0) {
                memoryEntries = getDataForMemoryLineChart(monitoringRecords);
                cpuEntries = getDataForCpuLineChart(monitoringRecords);
                diskEntries = getDataForDiskLineChart(monitoringRecords);
            }
        }
        List<Entry> finalMemoryEntries = memoryEntries;
        List<Entry> finalCpuEntries = cpuEntries;
        List<Entry> finalDiskEntries = diskEntries;
        ArrayList<MonitoringRecordEntity> finalMonitoringRecords = monitoringRecords;
        activity.runOnUiThread(() -> {
            if (serverModel.getMonitoringSessionId() == -1 || finalMonitoringRecords.size() == 0) {
                binding.lcMemory.setVisibility(View.GONE);
                binding.lcCpu.setVisibility(View.GONE);
                binding.lcStorage.setVisibility(View.GONE);
            } else {
                binding.lcMemory.setVisibility(View.VISIBLE);
                binding.lcCpu.setVisibility(View.VISIBLE);
                binding.lcStorage.setVisibility(View.VISIBLE);
                configureLineChart(binding.lcMemory, finalMemoryEntries);
                configureLineChart(binding.lcCpu, finalCpuEntries);
                configureLineChart(binding.lcStorage, finalDiskEntries);
            }
        });
    }
    public void configureLineChart(LineChart lc, List<Entry> data) {
        LineDataSet dataSet = new LineDataSet(data, "");
        styleLineChartDataSet(dataSet);
        lc.setData(new LineData(dataSet));
        customizeLineChart(lc);
        lc.invalidate();
    }
    public void customizeLineChart(LineChart lc) {
        lc.getXAxis().setEnabled(false);
        lc.getAxisRight().setEnabled(false);
        Description description = new Description();
        description.setText("Time");
        lc.setDescription(description);
        lc.setTouchEnabled(false);
    }
    public void styleLineChartDataSet(LineDataSet dataSet) {
        dataSet.setColor(Color.BLUE);
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(Color.BLUE);
        dataSet.setCircleRadius(4f);
    }
    public List<Entry> getDataForMemoryLineChart(ArrayList<MonitoringRecordEntity> records) {
        List<Entry> entries = new ArrayList<>();
        for (MonitoringRecordEntity record : records) {
            entries.add(new Entry(getTemporarySecondsFromUnixTimestamp(record.timeRecorded), record.memoryUsedMb));
        }
        return entries;
    }
    public float getTemporarySecondsFromUnixTimestamp(long time) {
        float newNumber =(float)( time - ((long)1672594200 * 1000) );
        float seconds = newNumber / 1000;
        return newNumber;
    }
    public List<Entry> getDataForCpuLineChart(ArrayList<MonitoringRecordEntity> records) {
        List<Entry> entries = new ArrayList<>();
        for (MonitoringRecordEntity record : records) {
            entries.add(new Entry(getTemporarySecondsFromUnixTimestamp(record.timeRecorded), (float)record.cpuUsagePercent));
        }
        return entries;
    }
    public List<Entry> getDataForDiskLineChart(ArrayList<MonitoringRecordEntity> records) {
        List<Entry> entries = new ArrayList<>();
        for (MonitoringRecordEntity record : records) {
            entries.add(new Entry(getTemporarySecondsFromUnixTimestamp(record.timeRecorded), (float)record.diskUsedMb));
        }
        return entries;
    }
    public void updatePieCharts() {
        float memoryUsed = serverModel.getMemoryUsedMb();
        float memoryTotal = serverModel.getMemoryTotalMb();
        float diskUsed = (float) serverModel.getDiskUsedMb();
        float diskTotal = (float) serverModel.getDiskTotalMb();
        float cpuUsed = (float) serverModel.getCpuUsagePercent();
        float cpuTotal = 100f;
        if (memoryTotal == 0) {
            activity.runOnUiThread(() -> {
                binding.pcMemory.setVisibility(View.GONE);
                binding.tvLabelMemoryNoData.setVisibility(View.VISIBLE);
            });
        } else {
            updatePieChart(binding.pcMemory, memoryUsed, memoryTotal, COLOR_LIGHT_YELLOW, COLOR_GREEN, binding.tvLabelMemoryNoData);
        }
        if (cpuUsed == -1) {
            activity.runOnUiThread(() -> {
                binding.pcCpu.setVisibility(View.GONE);
                binding.tvLabelCpuNoData.setVisibility(View.VISIBLE);
            });
        } else {
            updatePieChart(binding.pcCpu, cpuUsed, cpuTotal, COLOR_LIGHT_YELLOW, COLOR_GREEN, binding.tvLabelCpuNoData);
        }
        if (diskTotal == 0) {
            activity.runOnUiThread(() -> {
                binding.pcDisk.setVisibility(View.GONE);
                binding.tvLabelDiskNoData.setVisibility(View.VISIBLE);
            });
        } else {
            updatePieChart(binding.pcDisk, diskUsed, diskTotal, COLOR_LIGHT_YELLOW, COLOR_GREEN, binding.tvLabelDiskNoData);
        }
    }
    public void updatePieChart(PieChart pieChart, float usage, float total, int usageColor, int totalColor, TextView correspondingNoDataLabel) {
        PieData pieData = getPieData(
                usage,
                total,
                usageColor,
                totalColor
        );

        activity.runOnUiThread(() -> {
            stylePieChart(pieChart, pieData);
            correspondingNoDataLabel.setVisibility(View.GONE);
            pieChart.setVisibility(View.VISIBLE);
        });
    }
    public void stylePieChart(PieChart pieChart, PieData pieData) {
        Legend legend = pieChart.getLegend();
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setTextSize(14f);
        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawEntryLabels(false);
        pieChart.setDrawCenterText(false);
        pieChart.setHoleRadius(0f);
        pieChart.setTransparentCircleRadius(0);
        pieChart.setEntryLabelTextSize(14f);
        pieChart.invalidate();
    }

    public PieData getPieData(float usage, float total, int usageColor, int totalColor) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(usage, "used"));
        entries.add(new PieEntry(total - usage, "total"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(usageColor, totalColor);
        dataSet.setDrawValues(true);
        dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setValueLinePart1Length(1f);
        dataSet.setValueLinePart2Length(0.8f);
        dataSet.setValueTextSize(14f);
        return new PieData(dataSet);
    }


}