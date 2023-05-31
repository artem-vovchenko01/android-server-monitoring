package com.example.servermonitor.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "servers")
public class ServerEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;
    public String name;
    public String hostIp;
    public int port;
    public String userName;
    public String password;
    public int privateKeyId;

    public ServerEntity() {

    }
    @Ignore
    public ServerEntity(int id, String name, String hostIp, int port, String userName, String password, int privateKeyId) {
        this.id = id;
        this.name = name;
        this.hostIp = hostIp;
        this.port = port;
        this.userName = userName;
        this.password = password;
        this.privateKeyId = privateKeyId;
    }
}
