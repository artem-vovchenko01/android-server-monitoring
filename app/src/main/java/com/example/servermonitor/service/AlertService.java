package com.example.servermonitor.service;

import com.example.servermonitor.db.ServerDatabase;
import com.example.servermonitor.db.dao.AlertDao;
import com.example.servermonitor.db.entity.AlertEntity;
import com.example.servermonitor.mapper.AlertMapper;
import com.example.servermonitor.model.AlertModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AlertService {
    private AlertDao alertDao;
    private ServerDatabase database;
    public AlertService(ServerDatabase database) {
        this.database = database;
        this.alertDao = database.getAlertDao();
    }

    public ArrayList<AlertModel> getAllAlerts() {
        List<AlertEntity> alertEntities = alertDao.getAllAlerts();
        ArrayList<AlertModel> models = new ArrayList<>();
        for (AlertEntity entity : alertEntities) {
            models.add(AlertMapper.alertEntityToModel(entity));
        }
        return models;
    }

    public long addAlert(AlertModel alertModel) {
        return alertDao.addAlert(AlertMapper.alertModelToEntity(alertModel));
    }
    public AlertModel getAlertById(int id) {
        return AlertMapper.alertEntityToModel(alertDao.getAlert(id));
    }

    public void deleteAlert(AlertModel alertModel) {
        alertDao.deleteAlert(AlertMapper.alertModelToEntity(alertModel));
    }

    public void updateAlert(AlertModel alertModel) {
        alertDao.updateAlert(AlertMapper.alertModelToEntity(alertModel));
    }
}
