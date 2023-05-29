package com.example.servermonitor.service;

import com.example.servermonitor.db.entity.ServerEntity;
import com.example.servermonitor.mapper.ServerMapper;
import com.example.servermonitor.model.ServerModel;

import java.util.ArrayList;
import java.util.List;

public class ServerService {
    public static List<ServerModel> mapServers(List<ServerEntity> serverEntities) {
        ArrayList<ServerModel> serverModels = new ArrayList<>();
        for (ServerEntity serverEntity : serverEntities) {
            serverModels.add(ServerMapper.serverEntityToModel(serverEntity));
        }
        return serverModels;
    }
    public static void deleteServerById(int id) {

    }
}
