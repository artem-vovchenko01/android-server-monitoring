package com.example.servermonitor.service;

import com.example.servermonitor.db.ServerDatabase;
import com.example.servermonitor.db.dao.ShellScriptDao;
import com.example.servermonitor.db.entity.ShellScriptEntity;
import com.example.servermonitor.mapper.ShellScriptMapper;
import com.example.servermonitor.model.ShellScriptModel;

import java.util.ArrayList;
import java.util.List;

public class ShellScriptService {
    private ShellScriptDao shellScriptDao;
    private ServerDatabase database;
    public ShellScriptService(ServerDatabase database) {
        this.database = database;
        this.shellScriptDao = database.getShellScriptDao();
    }

    public ArrayList<ShellScriptModel> getAllShellScripts() {
        List<ShellScriptEntity> shellScriptEntities = shellScriptDao.getAllShellScripts();
        ArrayList<ShellScriptModel> models = new ArrayList<>();
        for (ShellScriptEntity entity : shellScriptEntities) {
            models.add(ShellScriptMapper.shellScriptToModel(entity));
        }
        return models;
    }

    public void addShellScript(ShellScriptModel shellScriptModel) {
        shellScriptDao.addShellScript(ShellScriptMapper.shellScriptToEntity(shellScriptModel));
    }

    public ShellScriptModel getShellScriptById(int id) {
        return ShellScriptMapper.shellScriptToModel(shellScriptDao.getShellScript(id));
    }

    public void deleteShellScript(ShellScriptModel shellScriptModel) {
        shellScriptDao.deleteShellScript(ShellScriptMapper.shellScriptToEntity(shellScriptModel));
    }
}
