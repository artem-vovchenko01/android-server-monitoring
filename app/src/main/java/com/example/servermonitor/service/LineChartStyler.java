package com.example.servermonitor.service;

import android.graphics.Color;

import com.example.servermonitor.db.entity.MonitoringRecordEntity;
import com.example.servermonitor.db.entity.MonitoringSessionEntity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
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

    public void styleLineChart(LineChart lineChart, ArrayList<MonitoringRecordEntity> monitoringData, LineChartDataType lineChartDataType, MonitoringSessionEntity session) {
        List<Entry> entryList = null;
        switch (lineChartDataType) {
            case DATA_MEMORY:
                entryList = getDataForMemoryLineChart(monitoringData, session);
                break;
            case DATA_CPU:
                entryList = getDataForCpuLineChart(monitoringData, session);
                break;
            case DATA_DISK:
                entryList = getDataForDiskLineChart(monitoringData, session);
                break;
        }
        configureLineChart(lineChart, entryList);
    }
    public void configureLineChart(LineChart lc, List<Entry> data) {
        LineDataSet dataSet = new LineDataSet(data, "");
        styleLineChartDataSet(dataSet);
        lc.setData(new LineData(dataSet));
        customizeLineChart(lc, dataSet);
    }
    public void customizeLineChart(LineChart lc, LineDataSet lineData) {
        lc.getXAxis().setEnabled(false);
        lc.getAxisRight().setEnabled(false);

        lc.getXAxis().setSpaceMin(0.5f);
        lc.getXAxis().setSpaceMax(0.5f);
        lc.getXAxis().setEnabled(true);
        lc.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        List<Float> minAdnMaxX = getMinAndMaxX(lineData);
        float startX = minAdnMaxX.get(0);
        float endX = minAdnMaxX.get(1);

        lc.getXAxis().setAxisMinimum(startX);
        lc.getXAxis().setAxisMaximum(endX);

        Description description = new Description();
        description.setText("Time");
        lc.setDescription(description);
        lc.setTouchEnabled(false);
    }
    private List<Float> getMinAndMaxX(LineDataSet data) {
        Entry entry = data.getValues().get(data.getValues().size() - 1);
        float from = 0;
        float to = 20;
        float lastX = entry.getX();
        if (lastX > 20) {
            to = lastX;
            from = lastX - 20;
        }
        ArrayList<Float> results = new ArrayList<>();
        results.add(from);
        results.add(to);
        return results;
    }
    public void styleLineChartDataSet(LineDataSet dataSet) {
        dataSet.setColor(Color.BLUE);
        dataSet.setLineWidth(2f);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        dataSet.setCircleRadius(4f);
    }
    public List<Entry> getDataForMemoryLineChart(ArrayList<MonitoringRecordEntity> records, MonitoringSessionEntity session) {
        List<Entry> entries = new ArrayList<>();
        for (MonitoringRecordEntity record : records) {
            entries.add(new Entry(getSecondsFromStart(record.timeRecorded, session), record.memoryUsedMb));
        }
        return entries;
    }
    public float getSecondsFromStart(long time, MonitoringSessionEntity session) {
        long difference = time - session.dateStarted;
        return (float) (difference / 1000);
    }
    public List<Entry> getDataForCpuLineChart(ArrayList<MonitoringRecordEntity> records, MonitoringSessionEntity session) {
        List<Entry> entries = new ArrayList<>();
        for (MonitoringRecordEntity record : records) {
            entries.add(new Entry(getSecondsFromStart(record.timeRecorded, session), (float)record.cpuUsagePercent));
        }
        return entries;
    }
    public List<Entry> getDataForDiskLineChart(ArrayList<MonitoringRecordEntity> records, MonitoringSessionEntity session) {
        List<Entry> entries = new ArrayList<>();
        for (MonitoringRecordEntity record : records) {
            entries.add(new Entry(getSecondsFromStart(record.timeRecorded, session), (float)record.diskUsedMb));
        }
        return entries;
    }
}
