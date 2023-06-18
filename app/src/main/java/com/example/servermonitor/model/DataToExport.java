package com.example.servermonitor.model;

import com.example.servermonitor.db.entity.AlertEntity;
import com.example.servermonitor.db.entity.MonitoringRecordEntity;
import com.example.servermonitor.db.entity.MonitoringSessionEntity;
import com.example.servermonitor.db.entity.ServerEntity;
import com.example.servermonitor.db.entity.ShellScriptEntity;
import com.example.servermonitor.db.entity.SshKeyEntity;

import java.util.List;

public class DataToExport {
    public List<AlertEntity> alerts;
    public List<MonitoringRecordEntity> records;
    public List<MonitoringSessionEntity> sessions;
    public List<ServerEntity> servers;
    public List<ShellScriptEntity> shellScripts;
    public List<SshKeyEntity> sshKeys;

    public DataToExport(List<AlertEntity> alerts, List<MonitoringRecordEntity> records, List<MonitoringSessionEntity> sessions, List<ServerEntity> servers, List<ShellScriptEntity> shellScripts, List<SshKeyEntity> sshKeys) {
        this.alerts = alerts;
        this.records = records;
        this.sessions = sessions;
        this.servers = servers;
        this.shellScripts = shellScripts;
        this.sshKeys = sshKeys;
    }
}
