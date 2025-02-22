package com.tyv.storageservice.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FileConstants {
    FROG("02a1b62e-0bd2-4f2e-9117-956018769552", "frog.png"),
    LIZARD("e402aecc-f2ce-4ace-8e43-00b93b79de3f", "lizard.png"),
    SNAKE("413d1071-0e54-4e68-9737-d0fff1a9d208", "snake.png"),
    MESSAGE("176c12cb-175f-464e-b2f9-b9d6bd77d1cb", "message.txt");

    static public final String SOURCE_DIR = "/files/";
    private final String uuid;
    private final String sourceName;

    public String getNameWithUUID() {
        return uuid + '-' + sourceName;
    }

    public String getSourcePath() {
        return SOURCE_DIR + sourceName;
    }
}
