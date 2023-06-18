package com.example.servermonitor.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.servermonitor.db.entity.AlertEntity;

import java.util.List;

@Dao
public interface AlertDao {
    @Insert
    long addAlert(AlertEntity alertEntity);

    @Update
    void updateAlert(AlertEntity alertEntity);

    @Delete
    void deleteAlert(AlertEntity alertEntity);

    @Query("delete from alerts where id == :id")
    void deleteAlertById(int id);
    @Query("delete from alerts;")
    void deleteAllAlerts();

    @Query("select * from alerts")
    List<AlertEntity> getAllAlerts();

    @Query("select * from alerts where id == :id")
    AlertEntity getAlert(int id);
}
