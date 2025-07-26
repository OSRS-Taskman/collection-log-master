package com.logmaster.domain.old;

import com.logmaster.domain.BaseSaveData;
import com.logmaster.domain.TaskTier;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
@ToString
@Deprecated
public class OldSaveData extends BaseSaveData {
    public final static Integer VERSION = null;

    public OldSaveData() {
        this.progress = new HashMap<>();

        for (TaskTier tier : TaskTier.values()) {
            this.progress.put(tier, new HashSet<>());
        }
    }

    private final Map<TaskTier, Set<Integer>> progress;

    @Setter
    private OldTaskPointer activeTaskPointer;

    @Setter
    private TaskTier selectedTier;
}
