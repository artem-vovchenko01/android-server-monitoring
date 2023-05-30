package com.example.servermonitor.mapper;

import com.example.servermonitor.db.entity.SshKeyEntity;
import com.example.servermonitor.model.SshKeyModel;

public class SshKeyMapper {
    public static SshKeyEntity sshKeyToEntity(SshKeyModel model) {
        return new SshKeyEntity(0, model.getName(), model.getKeyData());
    }

    public static SshKeyModel sshKeyToModel(SshKeyEntity entity) {
        return new SshKeyModel(entity.name, entity.keyData);
    }
}
