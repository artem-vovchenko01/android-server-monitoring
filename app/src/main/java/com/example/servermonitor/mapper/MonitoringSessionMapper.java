package com.example.servermonitor.mapper;

import com.example.servermonitor.db.Converters;
import com.example.servermonitor.db.entity.MonitoringSessionEntity;
import com.example.servermonitor.model.MonitoringSessionModel;

public class MonitoringSessionMapper {
    public static MonitoringSessionEntity monitoringSessionModelToEntity(MonitoringSessionModel model) {
        return new MonitoringSessionEntity(
                model.getId(),
                model.getName(),
                Converters.dateToTimestamp(model.getDateStarted()),
                model.getDateEnded() == null ? 0 : Converters.dateToTimestamp(model.getDateEnded()),
                model.getServerId()
        );
    }

    public static MonitoringSessionModel monitoringSessionEntityToModel(MonitoringSessionEntity entity) {
        return new MonitoringSessionModel(
                entity.id,
                entity.name,
                Converters.fromTimestamp(entity.dateStarted),
                Converters.fromTimestamp(entity.dateEnded),
                entity.serverId
        );
    }
}
