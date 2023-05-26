package com.example.servermonitor.service;

import com.example.servermonitor.db.ServerDatabase;
import com.example.servermonitor.db.entity.MonitoringRecordEntity;

import java.util.ArrayList;

public class MonitoringRecordService {
    public static ArrayList<MonitoringRecordEntity> getMonitoringRecordsByMonitoringSessionId(ServerDatabase database, int sessionId) {
        return new ArrayList<>(database.getMonitoringRecordDao().getAllByMonitoringSessionId(sessionId));
    }
}
