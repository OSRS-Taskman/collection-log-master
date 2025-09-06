package com.collectionlogmaster.domain.verification;

import com.collectionlogmaster.domain.verification.clog.CollectionLogVerification;
import com.collectionlogmaster.domain.verification.diary.AchievementDiaryVerification;
import com.collectionlogmaster.domain.verification.skill.SkillVerification;
import lombok.Getter;

@Getter
public abstract class Verification {
	private VerificationMethod method;

	public boolean isCollectionLog() {
		return this instanceof CollectionLogVerification;
	}

	public boolean isAchievementDiary() {
		return this instanceof AchievementDiaryVerification;
	}

	public boolean isSkill() {
		return this instanceof SkillVerification;
	}

	public CollectionLogVerification asCollectionLog() {
		return (CollectionLogVerification) this;
	}

	public AchievementDiaryVerification asAchievementDiary() {
		return (AchievementDiaryVerification) this;
	}

	public SkillVerification asSkill() {
		return (SkillVerification) this;
	}
}
