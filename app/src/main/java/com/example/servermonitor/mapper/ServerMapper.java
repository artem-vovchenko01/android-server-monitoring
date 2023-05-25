package com.example.servermonitor.mapper;

import com.example.servermonitor.model.ServerModel;
import com.example.servermonitor.db.entity.ServerEntity;

public class ServerMapper {
    public static ServerEntity serverModelToEntity(ServerModel serverModel) {
        return new ServerEntity(
                serverModel.getId(),
                serverModel.getName(),
                serverModel.getHostIp(),
                serverModel.getPort(),
                serverModel.getUserName(),
                serverModel.getPassword(),
                serverModel.getPrivateKey()
        );
    }

    public static ServerModel serverEntityToModel(ServerEntity serverEntity) {
        return new ServerModel(
               serverEntity.id,
               serverEntity.name,
               serverEntity.hostIp,
               serverEntity.port,
               serverEntity.userName,
               serverEntity.password,
               serverEntity.privateKey,
              false,
                0,
                0,
                0,
                0,
                0,
                0
        );
    }
}
