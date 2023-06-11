package com.example.servermonitor.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class AlertModel implements Parcelable {
    public enum AlertType {
        TYPE_MEMORY("Memory (MB)"),
        TYPE_CPU("CPU (%)"),
        TYPE_STORAGE("Storage (MB)");
        private String friendlyName;
        private AlertType(String friendlyName){
            this.friendlyName = friendlyName;
        }

        @Override public String toString(){
            return friendlyName;
        }
    }
    private int id;
    private String name;
    private AlertType alertType;
    private int thresholdValue;
    private int serverId;

    public AlertType getAlertType() {
        return alertType;
    }

    public void setAlertType(AlertType alertType) {
        this.alertType = alertType;
    }

    public AlertModel() {

    }

    public AlertModel(int id, String name, AlertType alertType, int thresholdValue, int serverId) {
        this.id = id;
        this.name = name;
        this.alertType = alertType;
        this.thresholdValue = thresholdValue;
        this.serverId = serverId;
    }

    protected AlertModel(Parcel in) {
        id = in.readInt();
        name = in.readString();
        alertType = AlertType.values()[in.readInt()];
        thresholdValue = in.readInt();
        serverId = in.readInt();
    }

    public static final Creator<AlertModel> CREATOR = new Creator<AlertModel>() {
        @Override
        public AlertModel createFromParcel(Parcel in) {
            return new AlertModel(in);
        }

        @Override
        public AlertModel[] newArray(int size) {
            return new AlertModel[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getThresholdValue() {
        return thresholdValue;
    }

    public void setThresholdValue(int thresholdValue) {
        this.thresholdValue = thresholdValue;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeInt(alertType.ordinal());
        dest.writeInt(thresholdValue);
        dest.writeInt(serverId);
    }
}
