package com.example.servermonitor.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.servermonitor.db.entity.MonitoringRecordEntity;

import java.util.List;

@Dao
public interface MonitoringRecordDao {
    @Insert
    void addMonitoringRecord(MonitoringRecordEntity monitoringRecordEntity);

    @Update
    void updateMonitoringRecord(MonitoringRecordEntity monitoringRecordEntity);

    @Delete
    void deleteMonitoringRecord(MonitoringRecordEntity monitoringRecordEntity);
    @Query("delete from monitoringRecords")
    void deleteAllMonitoringRecords();

    @Query("select * from monitoringRecords")
    List<MonitoringRecordEntity> getAllMonitoringRecords();

    @Query("select * from monitoringRecords where monitoringSessionId == :sessionId")
    List<MonitoringRecordEntity> getAllByMonitoringSessionId(int sessionId);

    @Query("select * from monitoringRecords where id == :id")
    MonitoringRecordEntity getMonitoringRecord(int id);
}
