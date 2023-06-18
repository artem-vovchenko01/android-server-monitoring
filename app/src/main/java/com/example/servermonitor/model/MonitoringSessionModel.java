package com.example.servermonitor.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.example.servermonitor.db.Converters;

import java.util.Date;

public class MonitoringSessionModel implements Parcelable {
    private int id;
    private String name;
    private Date dateStarted;
    private Date dateEnded;
    private int serverId;

    public MonitoringSessionModel() {

    }
    public MonitoringSessionModel(int id, String name, Date dateStarted, Date dateEnded, int serverId) {
        this.id = id;
        this.name = name;
        this.dateStarted = dateStarted;
        this.dateEnded = dateEnded;
        this.serverId = serverId;
    }

    protected MonitoringSessionModel(Parcel in) {
        id = in.readInt();
        name = in.readString();
        serverId = in.readInt();
        dateStarted = Converters.fromTimestamp(in.readLong());
        dateEnded = Converters.fromTimestamp(in.readLong());
    }

    public static final Creator<MonitoringSessionModel> CREATOR = new Creator<MonitoringSessionModel>() {
        @Override
        public MonitoringSessionModel createFromParcel(Parcel in) {
            return new MonitoringSessionModel(in);
        }

        @Override
        public MonitoringSessionModel[] newArray(int size) {
            return new MonitoringSessionModel[size];
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

    public Date getDateStarted() {
        return dateStarted;
    }

    public void setDateStarted(Date dateStarted) {
        this.dateStarted = dateStarted;
    }

    public Date getDateEnded() {
        return dateEnded;
    }

    public void setDateEnded(Date dateEnded) {
        this.dateEnded = dateEnded;
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
        dest.writeInt(serverId);
        dest.writeLong(Converters.dateToTimestamp(dateStarted));
        dest.writeLong(Converters.dateToTimestamp(dateEnded));
    }
}
