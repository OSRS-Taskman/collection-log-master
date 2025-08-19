package com.collectionlogmaster.synchronization.diary;

import com.collectionlogmaster.domain.Task;
import com.collectionlogmaster.domain.verification.diary.AchievementDiaryVerification;
import com.collectionlogmaster.domain.verification.diary.DiaryDifficulty;
import com.collectionlogmaster.domain.verification.diary.DiaryRegion;
import com.collectionlogmaster.synchronization.Verifier;
import lombok.NonNull;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AchievementDiaryVerifier implements Verifier {
    @Inject
    private AchievementDiaryService achievementDiaryService;

    public boolean supports(@NonNull Task task) {
        return task.getVerification() instanceof AchievementDiaryVerification;
    }

    public boolean verify(@NonNull Task task) {
        assert task.getVerification() instanceof AchievementDiaryVerification;
        AchievementDiaryVerification verif = (AchievementDiaryVerification) task.getVerification();

        DiaryRegion diary = verif.getRegion();
        DiaryDifficulty difficulty = verif.getDifficulty();

        return achievementDiaryService.isComplete(diary, difficulty);
    }
}
