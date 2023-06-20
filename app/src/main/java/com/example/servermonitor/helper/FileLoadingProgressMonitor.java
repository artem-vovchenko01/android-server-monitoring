package com.example.servermonitor.helper;

import com.jcraft.jsch.SftpProgressMonitor;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

public class FileLoadingProgressMonitor implements SftpProgressMonitor {
    private long totalBytes;
    private long transferredBytes;
    private double progress;;
    private String progressPercents = "0";
    private NumberFormat formatter = new DecimalFormat("#0.00", DecimalFormatSymbols.getInstance(Locale.US));
    private boolean totalBytesAlreadySet = false;
    public FileLoadingProgressMonitor() {

    }
    public FileLoadingProgressMonitor(long size) {
        totalBytes = size;
        totalBytesAlreadySet = true;
    }
    @Override
    public void init(int op, String src, String dest, long max) {
        if (! totalBytesAlreadySet)
            totalBytes = max;
    }

    @Override
    public boolean count(long count) {
        transferredBytes += count;
        progress = (double) transferredBytes/totalBytes;
        progressPercents =  formatter.format(progress*100);
        return true;
    }

    @Override
    public void end() {

    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public long getTransferredBytes() {
        return transferredBytes;
    }

    public double getProgress() {
        return progress;
    }

    public String getProgressPercents() {
        return progressPercents;
    }
}
