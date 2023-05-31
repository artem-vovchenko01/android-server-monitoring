package com.example.servermonitor.mapper;

import com.example.servermonitor.db.entity.ShellScriptEntity;
import com.example.servermonitor.model.ShellScriptModel;

public class ShellScriptMapper {
    public static ShellScriptEntity shellScriptToEntity(ShellScriptModel model) {
        return new ShellScriptEntity(model.getId(), model.getName(), model.getScriptData());
    }

    public static ShellScriptModel shellScriptToModel(ShellScriptEntity entity) {
        return new ShellScriptModel(entity.id, entity.name, entity.scriptData);
    }
}
