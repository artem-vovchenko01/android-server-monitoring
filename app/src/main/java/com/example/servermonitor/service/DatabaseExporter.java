package com.example.servermonitor.service;

import com.example.servermonitor.db.ServerDatabase;
import com.example.servermonitor.db.entity.AlertEntity;
import com.example.servermonitor.db.entity.MonitoringRecordEntity;
import com.example.servermonitor.db.entity.MonitoringSessionEntity;
import com.example.servermonitor.db.entity.ServerEntity;
import com.example.servermonitor.db.entity.ShellScriptEntity;
import com.example.servermonitor.db.entity.SshKeyEntity;
import com.example.servermonitor.model.DataToExport;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

public class DatabaseExporter {
    private ServerDatabase database;
    public DatabaseExporter(ServerDatabase database) {
        this.database = database;
    }
    public void clearDatabase() {
        database.getAlertDao().deleteAllAlerts();
        database.getMonitoringRecordDao().deleteAllMonitoringRecords();
        database.getMonitoringSessionDao().deleteAllMonitoringSessions();
        database.getServerDao().deleteAllServers();
        database.getShellScriptDao().deleteAllShellScripts();
        database.getSshKeyDao().deleteAllSshKeys();
    }

    public void importDatabaseData(String data) {
        clearDatabase();
        Gson gson = new GsonBuilder().create();
        DataToExport importedData = gson.fromJson(data, DataToExport.class);
        for (AlertEntity alert : importedData.alerts) {
            database.getAlertDao().addAlert(alert);
        }
        for (MonitoringRecordEntity record : importedData.records) {
            database.getMonitoringRecordDao().addMonitoringRecord(record);
        }
        for (MonitoringSessionEntity session : importedData.sessions) {
            database.getMonitoringSessionDao().addMonitoringSession(session);
        }
        for (ServerEntity server : importedData.servers) {
            database.getServerDao().addServer(server);
        }
        for (ShellScriptEntity shellScript : importedData.shellScripts) {
            database.getShellScriptDao().addShellScript(shellScript);
        }
        for (SshKeyEntity sshKey : importedData.sshKeys) {
            database.getSshKeyDao().addSshKey(sshKey);
        }
    }

    public String exportDatabaseData() {
        DataToExport dataToExport = getDataToExport();
        Gson gson = new GsonBuilder().create();
        return gson.toJson(dataToExport);
    }

    public DataToExport getDataToExport() {
        List<AlertEntity> alerts = database.getAlertDao().getAllAlerts();
        List<MonitoringRecordEntity> records = database.getMonitoringRecordDao().getAllMonitoringRecords();
        List<MonitoringSessionEntity> sessions = database.getMonitoringSessionDao().getAllMonitoringSessions();
        List<ServerEntity> servers = database.getServerDao().getAllServers();
        List<ShellScriptEntity> shellScripts = database.getShellScriptDao().getAllShellScripts();
        List<SshKeyEntity> sshKeys = database.getSshKeyDao().getAllSshKeys();
        return new DataToExport(alerts, records, sessions, servers, shellScripts, sshKeys);
    }
}
