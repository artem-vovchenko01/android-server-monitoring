package com.example.servermonitor.model;

public class ServerModel {
    private int id;
    private String name;
    private String hostIp;
    private int port;
    private String userName;
    private String password;
    private String privateKey;
    private boolean connected;
    private int memoryUsedMb;
    private int memoryTotalMb;
    private double cpuUsagePercent;
    private double diskUsedMb;
    private double diskTotalMb;
    private int serverStatusImg;
    private int monitoringSessionId = -1;

    public ServerModel(int id, String name, String hostIp, int port, String userName, String password, String privateKey, boolean connected, int memoryUsedMb, int memoryTotalMb, double cpuUsagePercent, double diskUsedMb, double diskTotalMb, int serverStatusImg) {
        this.id = id;
        this.name = name;
        this.hostIp = hostIp;
        this.port = port;
        this.userName = userName;
        this.password = password;
        this.privateKey = privateKey;
        this.connected = connected;
        this.memoryUsedMb = memoryUsedMb;
        this.memoryTotalMb = memoryTotalMb;
        this.cpuUsagePercent = cpuUsagePercent;
        this.diskUsedMb = diskUsedMb;
        this.diskTotalMb = diskTotalMb;
        this.serverStatusImg = serverStatusImg;
    }

    public int getMonitoringSessionId() {
        return monitoringSessionId;
    }

    public void setMonitoringSessionId(int monitoringSessionId) {
        this.monitoringSessionId = monitoringSessionId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHostIp() {
        return hostIp;
    }

    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public int getServerStatusImg() {
        return serverStatusImg;
    }

    public void setServerStatusImg(int serverStatusImg) {
        this.serverStatusImg = serverStatusImg;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public int getMemoryUsedMb() {
        return memoryUsedMb;
    }

    public void setMemoryUsedMb(int memoryUsedMb) {
        this.memoryUsedMb = memoryUsedMb;
    }

    public int getMemoryTotalMb() {
        return memoryTotalMb;
    }

    public void setMemoryTotalMb(int memoryTotalMb) {
        this.memoryTotalMb = memoryTotalMb;
    }

    public double getCpuUsagePercent() {
        return cpuUsagePercent;
    }

    public void setCpuUsagePercent(double cpuUsagePercent) {
        this.cpuUsagePercent = cpuUsagePercent;
    }

    public double getDiskUsedMb() {
        return diskUsedMb;
    }

    public void setDiskUsedMb(double diskUsedMb) {
        this.diskUsedMb = diskUsedMb;
    }

    public double getDiskTotalMb() {
        return diskTotalMb;
    }

    public void setDiskTotalMb(double diskTotalMb) {
        this.diskTotalMb = diskTotalMb;
    }
}
