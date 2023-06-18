package com.example.servermonitor.db.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "alerts"
)
public class AlertEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public int alertType;
    public int thresholdValue;
    public int serverId;

    public AlertEntity() {

    }

    public AlertEntity(int id, String name, int alertType, int thresholdValue, int serverId) {
        this.id = id;
        this.name = name;
        this.alertType = alertType;
        this.thresholdValue = thresholdValue;
        this.serverId = serverId;
    }
}
