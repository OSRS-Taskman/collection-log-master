package com.collectionlogmaster.taskapp;

import lombok.Data;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Data
@RequiredArgsConstructor
public class TaskAppState {
    private final boolean isLoggedIn;
    private final String activeTaskId;
    private final boolean isOfficial;
    private final boolean isLmsEnabled;
    @Accessors(fluent = true)
    private final boolean hasMigrated;
    private final Set<String> completedTasks;

    public TaskAppState() {
        this(false, null, true, true, true, Set.of());
    }
}
