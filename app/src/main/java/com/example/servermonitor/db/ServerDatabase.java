package com.example.servermonitor.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import com.example.servermonitor.db.dao.ServerDao;
import com.example.servermonitor.db.entity.ServerEntity;

@Database(entities = {ServerEntity.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class ServerDatabase extends RoomDatabase {
    public abstract ServerDao getServerDao();
}
