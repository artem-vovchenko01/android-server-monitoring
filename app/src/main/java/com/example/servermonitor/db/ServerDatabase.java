package com.example.servermonitor.db;

import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import com.example.servermonitor.db.dao.MonitoringRecordDao;
import com.example.servermonitor.db.dao.MonitoringSessionDao;
import com.example.servermonitor.db.dao.ServerDao;
import com.example.servermonitor.db.entity.MonitoringRecordEntity;
import com.example.servermonitor.db.entity.MonitoringSessionEntity;
import com.example.servermonitor.db.entity.ServerEntity;

@Database(entities = {ServerEntity.class,
        MonitoringRecordEntity.class,
        MonitoringSessionEntity.class},
        version = 2
)
@TypeConverters({Converters.class})
public abstract class ServerDatabase extends RoomDatabase {
    public abstract ServerDao getServerDao();
    public abstract MonitoringRecordDao getMonitoringRecordDao();
    public abstract MonitoringSessionDao getMonitoringSessionDao();
}
