package com.logmaster.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
@ToString
public class SaveData extends BaseSaveData {
    public final static int VERSION = 1;

    public SaveData() {
        this.version = VERSION;
        this.progress = new HashMap<>();

        for (TaskTier tier : TaskTier.values()) {
            this.progress.put(tier, new HashSet<>());
        }
    }

    private final Map<TaskTier, Set<String>> progress;

    @Setter
    private @Nullable TaskPointer activeTaskPointer;
}
