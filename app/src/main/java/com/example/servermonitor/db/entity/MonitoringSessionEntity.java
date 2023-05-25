package com.example.servermonitor.db.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class MonitoringSessionEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;

    @Ignore
    public MonitoringSessionEntity(int id, String name) {
        this.id = id;
        this.name = name;
    }
}
