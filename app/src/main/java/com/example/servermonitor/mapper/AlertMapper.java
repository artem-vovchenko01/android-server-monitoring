package com.example.servermonitor.mapper;

import com.example.servermonitor.db.entity.AlertEntity;
import com.example.servermonitor.model.AlertModel;

public class AlertMapper {
    public static AlertModel alertEntityToModel(AlertEntity alertEntity) {
        return new AlertModel(
                alertEntity.id,
                alertEntity.name,
                AlertModel.AlertType.values()[alertEntity.alertType],
                alertEntity.thresholdValue,
                alertEntity.serverId
        );
    }

    public static AlertEntity alertModelToEntity(AlertModel alertModel) {
        return new AlertEntity(
                alertModel.getId(),
                alertModel.getName(),
                alertModel.getAlertType().ordinal(),
                alertModel.getThresholdValue(),
                alertModel.getServerId()
        );
    }
}
