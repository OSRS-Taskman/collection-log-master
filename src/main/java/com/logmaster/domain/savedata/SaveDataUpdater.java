package com.logmaster.domain.savedata;

import com.google.gson.reflect.TypeToken;
import com.logmaster.domain.*;
import com.logmaster.domain.old.OldSaveData;
import com.logmaster.domain.old.OldTask;
import com.logmaster.domain.old.OldTaskPointer;
import com.logmaster.util.FileUtils;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

import static com.logmaster.util.GsonOverride.GSON;

@Singleton
@Slf4j
@SuppressWarnings("deprecation")
public class SaveDataUpdater {
    public static SaveData update(String json) {
        BaseSaveData base = GSON.fromJson(json, BaseSaveData.class);
        if (base == null) {
            return new SaveData();
        }

        if (base.getVersion() == OldSaveData.VERSION) {
            OldSaveData old = GSON.fromJson(json, OldSaveData.class);
            return update(old);
        }

        return GSON.fromJson(json, SaveData.class);
    }

    private static SaveData update(OldSaveData old) {
        SaveData updated = new SaveData();

        Type mapType = new TypeToken<Map<TaskTier, Map<Integer, String>>>() {}.getType();
        Map<TaskTier, Map<Integer, String>> v0MigrationData =
                FileUtils.loadResource("domain/savedata/v0-migration.json", mapType);;

        Map<TaskTier, Set<Integer>> oldProgress = old.getProgress();
        Map<TaskTier, Set<String>> newProgress = updated.getProgress();

        for (TaskTier tier : TaskTier.values()) {
            Set<Integer> oldTierData = oldProgress.get(tier);
            Set<String> newTierData = newProgress.get(tier);
            Map<Integer, String> tierMigrationData = v0MigrationData.get(tier);

            for (Integer oldTaskId : oldTierData) {
                if (tierMigrationData.containsKey(oldTaskId)) {
                    newTierData.add(tierMigrationData.get(oldTaskId));
                }
            }
        }

        OldTaskPointer oldTaskPointer = old.getActiveTaskPointer();
        if (oldTaskPointer != null) {
            OldTask oldTask = oldTaskPointer.getTask();
            String newTaskId = v0MigrationData.get(oldTaskPointer.getTaskTier()).get(oldTask.getId());
            Task newTask = new Task(newTaskId, oldTask.getDescription(), oldTask.getItemID(), null);
            updated.setActiveTaskPointer(new TaskPointer(oldTaskPointer.getTaskTier(), newTask));
        }

        return updated;
    }
}
