package com.example.servermonitor.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class ShellScriptModel implements Parcelable {
    private int id;
    private String name;
    private String scriptData;

    public ShellScriptModel(int id, String name, String scriptData) {
        this.id = id;
        this.name = name;
        this.scriptData = scriptData;
    }

    protected ShellScriptModel(Parcel in) {
        id = in.readInt();
        name = in.readString();
        scriptData = in.readString();
    }

    public static final Creator<ShellScriptModel> CREATOR = new Creator<ShellScriptModel>() {
        @Override
        public ShellScriptModel createFromParcel(Parcel in) {
            return new ShellScriptModel(in);
        }

        @Override
        public ShellScriptModel[] newArray(int size) {
            return new ShellScriptModel[size];
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

    public String getScriptData() {
        return scriptData;
    }

    public void setScriptData(String scriptData) {
        this.scriptData = scriptData;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(scriptData);
    }
}
