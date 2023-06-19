package com.example.servermonitor.model;

public class SystemdServiceModel {
    public enum SystemdServiceStatus {
        RUNNING,
        EXITED,
        OTHER
    };
    public String serviceName;
    public String serviceLongName;
    public String serviceLoaded;
    public String serviceActive;
    public String serviceRunning;
    public SystemdServiceStatus serviceStatus;

    public SystemdServiceModel(String serviceInfo) {
        serviceLongName = "";
        String[] split = serviceInfo.split("\\s+");
        serviceName = split[0];
        serviceLoaded = split[1];
        serviceActive = split[2];
        serviceRunning = split[3];
        for (int i = 4; i < split.length; i++) {
            serviceLongName = serviceLongName + " " + split[i];
        }
        switch (serviceRunning) {
            case "running":
                serviceStatus = SystemdServiceStatus.RUNNING;
                break;
            case "exited":
                serviceStatus = SystemdServiceStatus.EXITED;
                break;
            default:
               serviceStatus = SystemdServiceStatus.OTHER;
        }
    }
}
