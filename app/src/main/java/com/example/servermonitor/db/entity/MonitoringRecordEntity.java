package com.example.servermonitor.db.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "monitoringRecords",
        foreignKeys =
        @ForeignKey(entity = MonitoringSessionEntity.class, parentColumns = "id", childColumns = "monitoringSessionId")
)
public class MonitoringRecordEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int monitoringSessionId;
    public long timeRecorded;
    public int memoryUsedMb;
    public int memoryTotalMb;
    public double cpuUsagePercent;
    public double diskUsedMb;
    public double diskTotalMb;

    public MonitoringRecordEntity() {

    }

    @Ignore
    public MonitoringRecordEntity(int id, int monitoringSessionId, long timeRecorded, int memoryUsedMb, int memoryTotalMb, int cpuUsagePercent, double diskUsedMb, double diskTotalMb) {
        this.id = id;
        this.monitoringSessionId = monitoringSessionId;
        this.timeRecorded = timeRecorded;
        this.memoryUsedMb = memoryUsedMb;
        this.memoryTotalMb = memoryTotalMb;
        this.cpuUsagePercent = cpuUsagePercent;
        this.diskUsedMb = diskUsedMb;
        this.diskTotalMb = diskTotalMb;
    }
}
