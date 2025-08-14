package com.logmaster.domain.savedata.v2;

import com.logmaster.domain.Task;
import com.logmaster.domain.savedata.BaseSaveData;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

@Getter
@ToString
@Deprecated
public class V2SaveData extends BaseSaveData {
    public final static int VERSION = 2;

    public V2SaveData() {
        this.version = VERSION;
    }

    private final Set<String> completedTasks = new HashSet<>();

    @Setter
    private @Nullable Task activeTask = null;
}
