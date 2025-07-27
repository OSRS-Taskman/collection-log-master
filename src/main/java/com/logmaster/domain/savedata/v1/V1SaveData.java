package com.logmaster.domain.savedata.v1;

import com.logmaster.domain.TaskTier;
import com.logmaster.domain.savedata.BaseSaveData;
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
@Deprecated
public class V1SaveData extends BaseSaveData {
    public final static int VERSION = 1;

    public V1SaveData() {
        this.version = VERSION;
        this.progress = new HashMap<>();

        for (TaskTier tier : TaskTier.values()) {
            this.progress.put(tier, new HashSet<>());
        }
    }

    private final Map<TaskTier, Set<String>> progress;

    @Setter
    private @Nullable V1TaskPointer activeTaskPointer;
}
