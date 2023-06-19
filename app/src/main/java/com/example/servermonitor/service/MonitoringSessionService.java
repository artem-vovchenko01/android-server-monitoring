package com.example.servermonitor.service;

import com.example.servermonitor.db.Converters;
import com.example.servermonitor.db.ServerDatabase;
import com.example.servermonitor.db.dao.MonitoringSessionDao;
import com.example.servermonitor.db.entity.MonitoringSessionEntity;
import com.example.servermonitor.mapper.MonitoringSessionMapper;
import com.example.servermonitor.model.MonitoringSessionModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

public class MonitoringSessionService {
    private MonitoringSessionDao monitoringSessionDao;
    private ServerDatabase database;
    public MonitoringSessionModel getMonitoringSessionModelById(int id) {
        MonitoringSessionEntity session = monitoringSessionDao.getMonitoringSession(id);
        return MonitoringSessionMapper.monitoringSessionEntityToModel(session);
    }
    public void updateMonitoringSession(MonitoringSessionModel monitoringSession) {
        monitoringSessionDao.updateMonitoringSession(MonitoringSessionMapper.monitoringSessionModelToEntity(monitoringSession));
    }
    public void addMonitoringSession(MonitoringSessionModel monitoringSession) {
        monitoringSessionDao.addMonitoringSession(MonitoringSessionMapper.monitoringSessionModelToEntity(monitoringSession));
    }
    public MonitoringSessionModel getMonitoringSessionByStartTime(Date time) {
        MonitoringSessionEntity entity = monitoringSessionDao.getMonitoringSessionByStartTime(Converters.dateToTimestamp(time));
        return MonitoringSessionMapper.monitoringSessionEntityToModel(entity);
    }
    public MonitoringSessionService(ServerDatabase database) {
        this.database = database;
        this.monitoringSessionDao = database.getMonitoringSessionDao();
    }

    public ArrayList<MonitoringSessionModel> getMonitoringSessionsByServerId(int serverId) {
        ArrayList<MonitoringSessionModel> monitoringSessions = new ArrayList<>();
        for (MonitoringSessionEntity entity : monitoringSessionDao.getMonitoringSessionsByServerId(serverId)) {
            monitoringSessions.add(MonitoringSessionMapper.monitoringSessionEntityToModel(entity));
        }
        monitoringSessions.sort((first, second) -> second.getDateStarted().compareTo(first.getDateStarted()));
        return monitoringSessions;
    }

    public void deleteMonitoringSession(MonitoringSessionModel monitoringSession) {
        monitoringSessionDao.deleteMonitoringSession(
                MonitoringSessionMapper.monitoringSessionModelToEntity(monitoringSession)
        );
    }
}
