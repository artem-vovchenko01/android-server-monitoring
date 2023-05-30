package com.example.servermonitor.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sshKeys")
public class SshKeyEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public String keyData;

    public SshKeyEntity() {

    }

    public SshKeyEntity(int id, String name, String keyData) {
        this.id = id;
        this.name = name;
        this.keyData = keyData;
    }
}
