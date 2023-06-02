package com.example.servermonitor.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.servermonitor.db.entity.SshKeyEntity;

import java.util.List;

@Dao
public interface SshKeyDao {
    @Insert
    long addSshKey(SshKeyEntity sshKeyEntity);

    @Update
    void updateSshKey(SshKeyEntity sshKeyEntity);

    @Delete
    void deleteSshKey(SshKeyEntity sshKeyEntity);

    @Query("delete from sshKeys where id == :id")
    void deleteSshKeyById(int id);

    @Query("select * from sshKeys")
    List<SshKeyEntity> getAllSshKeys();

    @Query("select * from sshKeys where id == :id")
    SshKeyEntity getSshKey(int id);
}
