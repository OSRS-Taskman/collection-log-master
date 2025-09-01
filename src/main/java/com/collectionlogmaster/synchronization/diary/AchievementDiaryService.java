package com.collectionlogmaster.synchronization.diary;

import com.collectionlogmaster.domain.verification.diary.DiaryDifficulty;
import com.collectionlogmaster.domain.verification.diary.DiaryRegion;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import org.jetbrains.annotations.NotNull;

@Slf4j
@Singleton
public class AchievementDiaryService {
	private static final int DIARY_COMPLETION_INFO_SCRIPT_ID = 2200;

	private static final int COMPLETE_TASK_COUNT_OFFSET = 0;
	private static final int TOTAL_TASK_COUNT_OFFSET = 1;
	private static final int REWARD_COLLECTED_OFFSET = 2;

	@Inject
	private Client client;

	public boolean isComplete(@NonNull DiaryRegion diary, @NonNull DiaryDifficulty difficulty) {
		int[] stack = runScript(diary);

		return stack[difficulty.getStackOffset() + REWARD_COLLECTED_OFFSET] == 1;
	}

	public int getTotalTaskCount(@NonNull DiaryRegion diary, @NonNull DiaryDifficulty difficulty) {
		int[] stack = runScript(diary);

		return stack[difficulty.getStackOffset() + TOTAL_TASK_COUNT_OFFSET];
	}

	public int getCompleteTaskCount(@NonNull DiaryRegion diary, @NonNull DiaryDifficulty difficulty) {
		int[] stack = runScript(diary);

		return stack[difficulty.getStackOffset() + COMPLETE_TASK_COUNT_OFFSET];
	}

	private int[] runScript(@NotNull DiaryRegion diary) {
		// https://github.com/RuneStar/cs2-scripts/blob/master/scripts/%5Bproc%2Cdiary_completion_info%5D.cs2
		client.runScript(DIARY_COMPLETION_INFO_SCRIPT_ID, diary.getId());
		return client.getIntStack();
	}
}
