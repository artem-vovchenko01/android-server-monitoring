package com.example.servermonitor.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class ServerModel implements Parcelable {
    private int id;
    private String name;
    private String hostIp;
    private int port;
    private String userName;
    private String password;
    private boolean connected;
    private int memoryUsedMb;
    private int memoryTotalMb;
    private double cpuUsagePercent;
    private double diskUsedMb;
    private double diskTotalMb;
    private int serverStatusImg;
    private int monitoringSessionId = -1;
    private int privateKeyId;

    public int getPrivateKeyId() {
        return privateKeyId;
    }

    public void setPrivateKeyId(int privateKeyId) {
        this.privateKeyId = privateKeyId;
    }

    public ServerModel() {}
    public ServerModel(int id, String name, String hostIp, int port, String userName, String password, int privateKeyId, boolean connected, int memoryUsedMb, int memoryTotalMb, double cpuUsagePercent, double diskUsedMb, double diskTotalMb, int serverStatusImg) {
        this.id = id;
        this.name = name;
        this.hostIp = hostIp;
        this.port = port;
        this.userName = userName;
        this.password = password;
        this.privateKeyId = privateKeyId;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(hostIp);
        dest.writeInt(port);
        dest.writeString(userName);
        dest.writeString(password);
        dest.writeInt(privateKeyId);
    }

    public static final Parcelable.Creator<ServerModel> CREATOR = new Parcelable.Creator<ServerModel>() {
        @Override
        public ServerModel createFromParcel(Parcel source) {
            return new ServerModel(source);
        }

        @Override
        public ServerModel[] newArray(int size) {
            return new ServerModel[size];
        }
    };
    private ServerModel(Parcel in) {
        this.name = in.readString();
        this.hostIp = in.readString();
        this.port = in.readInt();
        this.userName = in.readString();
        this.password = in.readString();
        this.privateKeyId = in.readInt();
    }

    @NonNull
    @Override
    public String toString() {
        return this.name + " (" + this.hostIp + ")";
    }
}
