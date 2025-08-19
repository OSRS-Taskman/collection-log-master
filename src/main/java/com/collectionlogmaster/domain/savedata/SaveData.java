package com.collectionlogmaster.domain.savedata;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

@Getter
@ToString
public class SaveData extends BaseSaveData {
    public final static int VERSION = 3;

    public SaveData() {
        this.version = VERSION;
    }

    @Setter
    private @Nullable String activeTaskId = null;

    private final Set<String> completedTasks = new HashSet<>();
}
