package com.collectionlogmaster.synchronization.diary;

import com.collectionlogmaster.domain.Task;
import com.collectionlogmaster.domain.verification.diary.AchievementDiaryVerification;
import com.collectionlogmaster.domain.verification.diary.DiaryDifficulty;
import com.collectionlogmaster.domain.verification.diary.DiaryRegion;
import com.collectionlogmaster.synchronization.Verifier;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.NonNull;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AchievementDiaryVerifier implements Verifier<Map<DiaryRegion, Map<DiaryDifficulty, Boolean>>> {
    @Inject
    private AchievementDiaryService achievementDiaryService;

    public boolean supports(@NonNull Task task) {
        return task.getVerification() instanceof AchievementDiaryVerification;
    }

    public boolean verify(@NonNull Task task) {
        assert task.getVerification() instanceof AchievementDiaryVerification;
        AchievementDiaryVerification verif = (AchievementDiaryVerification) task.getVerification();

        DiaryRegion region = verif.getRegion();
        DiaryDifficulty difficulty = verif.getDifficulty();

        return achievementDiaryService.isComplete(region, difficulty);
    }

    public Map<DiaryRegion, Map<DiaryDifficulty, Boolean>> verificationData() {
        return Arrays.stream(DiaryRegion.values())
            .collect(Collectors.toMap(
                region -> region,
				this::verificationData
            ));
    }

    private Map<DiaryDifficulty, Boolean> verificationData(DiaryRegion region) {
        return Arrays.stream(DiaryDifficulty.values())
            .collect(Collectors.toMap(
                difficulty -> difficulty,
				difficulty -> achievementDiaryService.isComplete(region, difficulty)
            ));
    }
}
