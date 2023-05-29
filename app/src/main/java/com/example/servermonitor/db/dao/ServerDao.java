package com.example.servermonitor.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.servermonitor.db.entity.ServerEntity;

import java.util.List;

@Dao
public interface ServerDao {
    @Insert
    void addServer(ServerEntity serverEntity);

    @Update
    void updateServer(ServerEntity serverEntity);

    @Delete
    void deleteServer(ServerEntity serverEntity);
    @Query("delete from servers where id == :id")
    void deleteServerById(int id);

    @Query("select * from servers")
    List<ServerEntity> getAllServers();

    @Query("select * from servers where id == :id")
    ServerEntity getServer(int id);
}
