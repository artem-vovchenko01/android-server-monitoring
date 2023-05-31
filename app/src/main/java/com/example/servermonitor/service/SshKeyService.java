package com.example.servermonitor.service;

import com.example.servermonitor.db.ServerDatabase;
import com.example.servermonitor.db.dao.SshKeyDao;
import com.example.servermonitor.db.entity.SshKeyEntity;
import com.example.servermonitor.mapper.SshKeyMapper;
import com.example.servermonitor.model.ServerModel;
import com.example.servermonitor.model.SshKeyModel;

import java.util.ArrayList;
import java.util.List;

public class SshKeyService {
    private SshKeyDao sshKeyDao;
    private ServerDatabase database;
    public SshKeyService(ServerDatabase database) {
        this.database = database;
        this.sshKeyDao = database.getSshKeyDao();
    }



    public ArrayList<SshKeyModel> getAllSshKeys() {
        List<SshKeyEntity> sshKeyEntities = sshKeyDao.getAllSshKeys();
        ArrayList<SshKeyModel> models = new ArrayList<>();
        for (SshKeyEntity entity : sshKeyEntities) {
            models.add(SshKeyMapper.sshKeyToModel(entity));
        }
        return models;
    }

    public void addSshKey(SshKeyModel sshKeyModel) {
        sshKeyDao.addSshKey(SshKeyMapper.sshKeyToEntity(sshKeyModel));
    }
    public SshKeyModel getSshKeyById(int id) {
        return SshKeyMapper.sshKeyToModel(sshKeyDao.getSshKey(id));
    }

    public void deleteSshKey(SshKeyModel sshKey) {
        sshKeyDao.deleteSshKey(SshKeyMapper.sshKeyToEntity(sshKey));
    }
}
