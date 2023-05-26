package com.example.servermonitor.db.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "monitoringSessions")
public class MonitoringSessionEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public long dateStarted;
    public long dateEnded;
    public MonitoringSessionEntity() {

    }

    @Ignore
    public MonitoringSessionEntity(int id, String name, long dateStarted, long dateEnded) {
        this.id = id;
        this.name = name;
        this.dateStarted = dateStarted;
        this.dateEnded = dateEnded;
    }
}
