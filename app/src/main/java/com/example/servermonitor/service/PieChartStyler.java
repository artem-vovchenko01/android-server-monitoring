package com.example.servermonitor.service;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;

public class PieChartStyler {
    private String labelPart;
    private String labelTotal;
    private int colorPart;
    private int colorTotal;
    public PieChartStyler(String labelPart, String labelTotal, int colorPart, int colorTotal) {
        this.labelPart = labelPart;
        this.labelTotal = labelTotal;
        this.colorPart = colorPart;
        this.colorTotal = colorTotal;
    }

    public void stylePieChart(PieChart pieChart, float part, float total) {
        PieData pieData = getPieData(part, total);
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
    }

    public PieData getPieData(float part, float total) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(part, labelPart));
        entries.add(new PieEntry(total - part, labelTotal));
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colorPart, colorTotal);
        dataSet.setDrawValues(true);
        dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setValueLinePart1Length(1f);
        dataSet.setValueLinePart2Length(0.8f);
        dataSet.setValueTextSize(14f);
        return new PieData(dataSet);
    }
}
