package com.example.servermonitor.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.servermonitor.MainActivity;
import com.example.servermonitor.R;
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

public class ServerActivity extends AppCompatActivity {
    private static int COLOR_LIGHT_YELLOW;
    private static int COLOR_GREEN;
    private static int CHART_REFRESH_INTERVAL = 5;
    Button btnOpenTerminal;
    Button btnChangeMonitoringState;
    PieChart pieChartMemory;
    PieChart pieChartCpu;
    PieChart pieChartDisk;
    Toolbar toolbar;
    LineChart lcMemory;
    LineChart lcCpu;
    LineChart lcStorage;
    public static ServerModel serverModel;
    public ServerDatabase database;
    TextView tvLabelMemoryNoData;
    TextView tvLabelCpuNoData;
    TextView tvLabelDiskNoData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        database = MainActivity.database;
        initializeUiComponents();
        setupOnClickListeners();
        // setSupportActionBar(toolbar);
        // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        monitorServer();
    }
    public void initializeUiComponents() {
        COLOR_LIGHT_YELLOW = ContextCompat.getColor(getApplicationContext(), R.color.light_yellow);
        COLOR_GREEN = ContextCompat.getColor(getApplicationContext(), R.color.pale_green);
        btnOpenTerminal = findViewById(R.id.btnOpenTerminal);
        btnChangeMonitoringState = findViewById(R.id.btnChangeMonitoringState);
        pieChartMemory = findViewById(R.id.pcMemory);
        pieChartCpu = findViewById(R.id.pcCpu);
        pieChartDisk = findViewById(R.id.pcDisk);
        lcMemory = findViewById(R.id.lcMemory);
        lcCpu = findViewById(R.id.lcCpu);
        lcStorage = findViewById(R.id.lcStorage);
        toolbar = findViewById(R.id.toolbar);
        tvLabelMemoryNoData = findViewById(R.id.tvLabelMemoryNoData);
        tvLabelCpuNoData = findViewById(R.id.tvLabelCpuNoData);
        tvLabelDiskNoData = findViewById(R.id.tvLabelDiskNoData);
        tvLabelMemoryNoData.setVisibility(View.GONE);
        tvLabelCpuNoData.setVisibility(View.GONE);
        tvLabelDiskNoData.setVisibility(View.GONE);
        updateMonitoringUiComponents();
    }
    public void setupOnClickListeners() {
        btnChangeMonitoringState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (serverModel.getMonitoringSessionId() != -1) {
                    btnChangeMonitoringState.setText("Start monitoring session");
                    saveMonitoringSessionToDb();
                } else {
                    btnChangeMonitoringState.setText("Stop monitoring session");
                    startNewMonitoringSession();
                }
            }
        });

        btnOpenTerminal.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), TerminalActivity.class);
            TerminalActivity.serverModel = serverModel;
            startActivity(intent);
        });
    }
    public void updateMonitoringUiComponents() {
        if (serverModel.getMonitoringSessionId() == -1) {
            btnChangeMonitoringState.setText("Start monitoring session");
        } else {
            btnChangeMonitoringState.setText("Stop monitoring session");
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
        runOnUiThread(() -> {
            if (serverModel.getMonitoringSessionId() == -1 || finalMonitoringRecords.size() == 0) {
                lcMemory.setVisibility(View.GONE);
                lcCpu.setVisibility(View.GONE);
                lcStorage.setVisibility(View.GONE);
            } else {
                lcMemory.setVisibility(View.VISIBLE);
                lcCpu.setVisibility(View.VISIBLE);
                lcStorage.setVisibility(View.VISIBLE);
                configureLineChart(lcMemory, finalMemoryEntries);
                configureLineChart(lcCpu, finalCpuEntries);
                configureLineChart(lcStorage, finalDiskEntries);
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
            runOnUiThread(() -> {
                pieChartMemory.setVisibility(View.GONE);
                tvLabelMemoryNoData.setVisibility(View.VISIBLE);
            });
        } else {
            updatePieChart(pieChartMemory, memoryUsed, memoryTotal, COLOR_LIGHT_YELLOW, COLOR_GREEN, tvLabelMemoryNoData);
        }
        if (cpuUsed == -1) {
            runOnUiThread(() -> {
                pieChartCpu.setVisibility(View.GONE);
                tvLabelCpuNoData.setVisibility(View.VISIBLE);
            });
        } else {
            updatePieChart(pieChartCpu, cpuUsed, cpuTotal, COLOR_LIGHT_YELLOW, COLOR_GREEN, tvLabelCpuNoData);
        }
        if (diskTotal == 0) {
            runOnUiThread(() -> {
                pieChartDisk.setVisibility(View.GONE);
                tvLabelDiskNoData.setVisibility(View.VISIBLE);
            });
        } else {
            updatePieChart(pieChartDisk, diskUsed, diskTotal, COLOR_LIGHT_YELLOW, COLOR_GREEN, tvLabelDiskNoData);
        }
    }
    public void updatePieChart(PieChart pieChart, float usage, float total, int usageColor, int totalColor, TextView correspondingNoDataLabel) {
        PieData pieData = getPieData(
                usage,
                total,
                usageColor,
                totalColor
        );

        runOnUiThread(() -> {
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}