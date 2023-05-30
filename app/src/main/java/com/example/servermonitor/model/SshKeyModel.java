package com.example.servermonitor.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class SshKeyModel implements Parcelable {
    private String name;
    private String keyData;

    public SshKeyModel(String name, String keyData) {
        this.name = name;
        this.keyData = keyData;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKeyData() {
        return keyData;
    }

    public void setKeyData(String keyData) {
        this.keyData = keyData;
    }

    protected SshKeyModel(Parcel in) {
        name = in.readString();
        keyData = in.readString();
    }

    public static final Creator<SshKeyModel> CREATOR = new Creator<SshKeyModel>() {
        @Override
        public SshKeyModel createFromParcel(Parcel in) {
            return new SshKeyModel(in);
        }

        @Override
        public SshKeyModel[] newArray(int size) {
            return new SshKeyModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(keyData);
    }
}
