package com.logmaster.synchronization.diary;

import com.logmaster.domain.verification.diary.DiaryDifficulty;
import com.logmaster.domain.verification.diary.DiaryRegion;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton
public class AchievementDiaryService {
    private static final int DIARY_COMPLETION_SCRIPT = 2200;

    @Inject
    private Client client;

    // Code from: RuneProfile
    // Repository: https://github.com/ReinhardtR/runeprofile-plugin
    // License: BSD 2-Clause License
    public boolean isComplete(@NonNull DiaryRegion diary, @NonNull DiaryDifficulty difficulty) {
        // https://github.com/RuneStar/cs2-scripts/blob/master/scripts/%5Bproc%2Cdiary_completion_info%5D.cs2
        client.runScript(DIARY_COMPLETION_SCRIPT, diary.getId());
        int[] stack = client.getIntStack();

        return stack[difficulty.getStackOffset()] == 1;
    }
}
