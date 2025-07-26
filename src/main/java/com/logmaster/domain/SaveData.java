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
    public SaveData() {
        this.version = 1;
        this.progress = new HashMap<>();

        for (TaskTier tier : TaskTier.values()) {
            this.progress.put(tier, new HashSet<>());
        }
    }

    public @Nullable Task currentTask;

    private final Map<TaskTier, Set<String>> progress;

    @Setter
    private @Nullable TaskPointer activeTaskPointer;
}
