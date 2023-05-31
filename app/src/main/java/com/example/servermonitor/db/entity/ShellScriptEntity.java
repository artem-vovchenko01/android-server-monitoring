package com.example.servermonitor.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "shellScripts")
public class ShellScriptEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;
    public String name;
    public String scriptData;

    public ShellScriptEntity() {

    }

    public ShellScriptEntity(int id, String name, String scriptData) {
        this.id = id;
        this.name = name;
        this.scriptData = scriptData;
    }
}
