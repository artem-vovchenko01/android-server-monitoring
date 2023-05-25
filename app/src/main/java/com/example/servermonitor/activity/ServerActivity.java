package com.example.servermonitor.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.example.servermonitor.R;
import com.example.servermonitor.model.ServerModel;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServerActivity extends AppCompatActivity {
    Button btnOpenTerminal;
    PieChart pieChartMemory;
    public static ServerModel serverModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        pieChartMemory = findViewById(R.id.pcMemory);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btnOpenTerminal = findViewById(R.id.btnOpenTerminal);

        monitorServer();


        btnOpenTerminal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), TerminalActivity.class);
                TerminalActivity.serverModel = serverModel;
                startActivity(intent);
            }
        });
    }

    public void monitorServer() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        Handler handler = new Handler(Looper.getMainLooper());
        executor.scheduleAtFixedRate(() -> {
            updatePieCharts();
        }, 0, 15, TimeUnit.SECONDS);
    }
    public void updatePieCharts() {
        float memoryUsed = serverModel.getMemoryUsedMb();
        float memoryTotal = serverModel.getMemoryTotalMb();
        float diskUsed = (float) serverModel.getDiskUsedMb();
        float diskTotal = (float) serverModel.getDiskTotalMb();
        float cpuUsed = (float) serverModel.getCpuUsagePercent();
        float cpuTotal = 100f;
        PieData memoryData = getPieData(
                memoryUsed,
                memoryTotal,
                ContextCompat.getColor(getApplicationContext(), R.color.light_yellow),
                ContextCompat.getColor(getApplicationContext(), R.color.pale_green)
        );

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stylePieChart(pieChartMemory, memoryData);
            }
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
        dataSet.setValueLinePart1Length(0.8f);
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