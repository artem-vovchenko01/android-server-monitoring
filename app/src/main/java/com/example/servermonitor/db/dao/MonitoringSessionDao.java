package com.example.servermonitor.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.servermonitor.db.entity.MonitoringSessionEntity;

import java.util.List;

@Dao
public interface MonitoringSessionDao {
    @Insert
    void addMonitoringSession(MonitoringSessionEntity monitoringRecordEntity);

    @Update
    void updateMonitoringSession(MonitoringSessionEntity monitoringRecordEntity);

    @Delete
    void deleteMonitoringSession(MonitoringSessionEntity monitoringRecordEntity);

    @Query("select * from monitoringSessions")
    List<MonitoringSessionEntity> getAllMonitoringSessions();

    @Query("select * from monitoringSessions where id == :id")
    MonitoringSessionEntity getMonitoringSession(int id);

    @Query("select * from monitoringSessions where dateStarted == :timestamp")
    MonitoringSessionEntity getMonitoringSessionByStartTime(long timestamp);
}
