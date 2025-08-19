package com.collectionlogmaster.domain.savedata.v0;

import com.collectionlogmaster.domain.TaskTier;
import com.collectionlogmaster.domain.savedata.BaseSaveData;
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
public class V0SaveData extends BaseSaveData {
    public final static Integer VERSION = null;

    public V0SaveData() {
        this.progress = new HashMap<>();

        for (TaskTier tier : TaskTier.values()) {
            this.progress.put(tier, new HashSet<>());
        }
    }

    private final Map<TaskTier, Set<Integer>> progress;

    @Setter
    private V0TaskPointer activeTaskPointer;

    @Setter
    private TaskTier selectedTier;
}
