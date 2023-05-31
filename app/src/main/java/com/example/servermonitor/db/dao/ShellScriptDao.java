package com.example.servermonitor.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.servermonitor.db.entity.ShellScriptEntity;

import java.util.List;

@Dao
public interface ShellScriptDao {
    @Insert
    long addShellScript(ShellScriptEntity shellScriptEntity);

    @Update
    void updateShellScript(ShellScriptEntity shellScriptEntity);

    @Delete
    void deleteShellScript(ShellScriptEntity shellScriptEntity);

    @Query("delete from shellScripts where id == :id")
    void deleteShellScriptById(int id);

    @Query("select * from shellScripts")
    List<ShellScriptEntity> getAllShellScripts();

    @Query("select * from shellScripts where id == :id")
    ShellScriptEntity getShellScript(int id);
}
