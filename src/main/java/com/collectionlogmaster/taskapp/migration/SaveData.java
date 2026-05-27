package com.collectionlogmaster.taskapp.migration;

import com.google.gson.annotations.SerializedName;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
public class SaveData {
	@Setter
    private Integer version = 3;

    @Setter
    @SerializedName("activeTaskId")
    private String activeTaskId = null;

    @Setter
    @SerializedName("migratedTaskId")
    private String migratedTaskId = null;

    @Setter
    private boolean migrated = false;

    @SerializedName("completedTasks")
    private final Set<String> completedTasks = new HashSet<>();
}