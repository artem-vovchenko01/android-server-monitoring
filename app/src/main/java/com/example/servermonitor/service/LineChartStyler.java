package com.example.servermonitor.service;

import android.graphics.Color;

import com.example.servermonitor.db.Converters;
import com.example.servermonitor.db.entity.MonitoringRecordEntity;
import com.example.servermonitor.model.MonitoringSessionModel;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class LineChartStyler {
    private static final int SHOWN_INTERVAL_SECONDS = 20;
    public enum LineChartDataType {
        DATA_MEMORY,
        DATA_CPU,
        DATA_DISK
    }

    public void styleLineChart(LineChart lineChart, ArrayList<MonitoringRecordEntity> monitoringData, LineChartDataType lineChartDataType, MonitoringSessionModel session, boolean activelyUpdating) {
        List<Entry> entryList = null;
        switch (lineChartDataType) {
            case DATA_MEMORY:
                entryList = getDataForMemoryLineChart(monitoringData, session);
                configureLineChart(monitoringData, lineChartDataType, lineChart, entryList, activelyUpdating);
                break;
            case DATA_CPU:
                entryList = getDataForCpuLineChart(monitoringData, session);
                configureLineChart(monitoringData, lineChartDataType, lineChart, entryList, activelyUpdating);
                break;
            case DATA_DISK:
                entryList = getDataForDiskLineChart(monitoringData, session);
                configureLineChart(monitoringData, lineChartDataType, lineChart, entryList, activelyUpdating);
                break;
        }
    }
    public void configureLineChart(ArrayList<MonitoringRecordEntity> records, LineChartDataType type, LineChart lc, List<Entry> data, boolean activelyUpdating) {
        LineDataSet dataSet = new LineDataSet(data, "");
        styleLineChartDataSet(dataSet);
        lc.setData(new LineData(dataSet));
        customizeLineChart(records, type, lc, dataSet, activelyUpdating);
    }
    public void customizeLineChart(ArrayList<MonitoringRecordEntity> records, LineChartDataType type, LineChart lc, LineDataSet lineData, boolean activelyUpdating) {
        lc.getAxisRight().setEnabled(false);
        // lc.getXAxis().setEnabled(true);

        //lc.getXAxis().setSpaceMin(0.5f);
        //lc.getXAxis().setSpaceMax(0.5f);
        lc.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        //lc.setDragEnabled(true);
        //lc.setScaleEnabled(false);

        List<Float> minAdnMaxX = getMinAndMaxX(lineData);
        float startX = minAdnMaxX.get(0);
        float endX = minAdnMaxX.get(1);
        if (activelyUpdating) {
            lc.getXAxis().setAxisMinimum(startX);
            lc.getXAxis().setAxisMaximum(endX);
        } else {
            lc.moveViewToX(startX);
            lc.setMaxVisibleValueCount(20);
        }

        lc.setVisibleXRangeMaximum(20);
        //lc.moveViewToX(startX);

        lc.setNoDataTextColor(Color.BLUE);

        if (records.size() > 0) {
            setYAxisRange(records, type, lc);
        }

        Description description = new Description();
        description.setText("Seconds");
        lc.setDescription(description);
        lc.setTouchEnabled(true);
        Legend legend = lc.getLegend();
        legend.setEnabled(false);
    }
    private void setYAxisRange(ArrayList<MonitoringRecordEntity> records, LineChartDataType type, LineChart lc) {
        MonitoringRecordEntity record = records.get(0);
        switch (type) {
            case DATA_MEMORY:
                lc.getAxisLeft().setAxisMaximum(record.memoryTotalMb);
                lc.getAxisLeft().setAxisMinimum(0);
                break;
            case DATA_CPU:
                lc.getAxisLeft().setAxisMaximum(100);
                lc.getAxisLeft().setAxisMinimum(0);
                break;
            case DATA_DISK:
                lc.getAxisLeft().setAxisMaximum((float) record.diskTotalMb);
                lc.getAxisLeft().setAxisMinimum(0);
                break;
        }
    }
    private List<Float> getMinAndMaxX(LineDataSet data) {
        int showItems = SHOWN_INTERVAL_SECONDS;
        float from = 0;
        float to = showItems;
        if (data.getValues().size() > 0) {
            Entry entry = data.getValues().get(data.getValues().size() - 1);
            float lastX = entry.getX();
            if (lastX > showItems) {
                to = lastX;
                from = lastX - showItems;
            }
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
    public List<Entry> getDataForMemoryLineChart(ArrayList<MonitoringRecordEntity> records, MonitoringSessionModel session) {
        List<Entry> entries = new ArrayList<>();
        for (MonitoringRecordEntity record : records) {
            entries.add(new Entry(getSecondsFromStart(record.timeRecorded, session), record.memoryUsedMb));
        }
        return entries;
    }
    public float getSecondsFromStart(long time, MonitoringSessionModel session) {
        long difference = time - Converters.dateToTimestamp(session.getDateStarted());
        return (float) (difference / 1000);
    }
    public List<Entry> getDataForCpuLineChart(ArrayList<MonitoringRecordEntity> records, MonitoringSessionModel session) {
        List<Entry> entries = new ArrayList<>();
        for (MonitoringRecordEntity record : records) {
            entries.add(new Entry(getSecondsFromStart(record.timeRecorded, session), (float)record.cpuUsagePercent));
        }
        return entries;
    }
    public List<Entry> getDataForDiskLineChart(ArrayList<MonitoringRecordEntity> records, MonitoringSessionModel session) {
        List<Entry> entries = new ArrayList<>();
        for (MonitoringRecordEntity record : records) {
            entries.add(new Entry(getSecondsFromStart(record.timeRecorded, session), (float)record.diskUsedMb));
        }
        return entries;
    }
}
