package com.collectionlogmaster.taskapp.request;

import com.collectionlogmaster.domain.verification.diary.DiaryDifficulty;
import com.collectionlogmaster.domain.verification.diary.DiaryRegion;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import net.runelite.api.Skill;

@Data
public class SyncRequest {
	private final Set<Integer> collectionLog;
	private final Map<DiaryRegion, Map<DiaryDifficulty, Boolean>> diaries;
	private final Map<Skill, Integer> skills;
}
