package com.example.servermonitor.service;

import com.example.servermonitor.db.ServerDatabase;
import com.example.servermonitor.db.dao.ServerDao;
import com.example.servermonitor.db.entity.ServerEntity;
import com.example.servermonitor.mapper.ServerMapper;
import com.example.servermonitor.model.ServerModel;

import java.util.ArrayList;
import java.util.List;

public class ServerService {
    private ServerDatabase database;
    private ServerDao serverDao;
    public ServerService(ServerDatabase database) {
        this.database = database;
        this.serverDao = database.getServerDao();
    }
    public ArrayList<ServerModel> getAllServers() {
        ArrayList<ServerModel> models = new ArrayList<>();
        List<ServerEntity> entities = serverDao.getAllServers();
        for (ServerEntity serverEntity : entities) {
            models.add(ServerMapper.serverEntityToModel(serverEntity));
        }
        return models;
    }
    public void deleteServerById(int id) {
        serverDao.deleteServerById(id);
    }
    public void deleteServer(ServerModel model) {
        serverDao.deleteServer(ServerMapper.serverModelToEntity(model));
    }
}
