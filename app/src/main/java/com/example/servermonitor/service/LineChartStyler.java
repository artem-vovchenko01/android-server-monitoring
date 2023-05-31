package com.example.servermonitor.service;

import android.graphics.Color;

import com.example.servermonitor.db.entity.MonitoringRecordEntity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class LineChartStyler {
    public enum LineChartDataType {
        DATA_MEMORY,
        DATA_CPU,
        DATA_DISK
    }

    public void styleLineChart(LineChart lineChart, ArrayList<MonitoringRecordEntity> monitoringData, LineChartDataType lineChartDataType) {
        List<Entry> entryList = null;
        switch (lineChartDataType) {
            case DATA_MEMORY:
                entryList = getDataForMemoryLineChart(monitoringData);
                break;
            case DATA_CPU:
                entryList = getDataForCpuLineChart(monitoringData);
                break;
            case DATA_DISK:
                entryList = getDataForDiskLineChart(monitoringData);
                break;
        }
        configureLineChart(lineChart, entryList);
    }
    public void configureLineChart(LineChart lc, List<Entry> data) {
        LineDataSet dataSet = new LineDataSet(data, "");
        styleLineChartDataSet(dataSet);
        lc.setData(new LineData(dataSet));
        customizeLineChart(lc);
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
}
