package com.collectionlogmaster.synchronization.skill;

import com.collectionlogmaster.domain.Task;
import com.collectionlogmaster.domain.verification.skill.SkillVerification;
import com.collectionlogmaster.synchronization.Verifier;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.NonNull;
import net.runelite.api.Client;
import net.runelite.api.Skill;

@Singleton
public class SkillVerifier implements Verifier<Map<Skill, Integer>> {
	@Inject
	private Client client;

	public boolean supports(@NonNull Task task) {
		return task.getVerification() instanceof SkillVerification;
	}

	public boolean verify(@NonNull Task task) {
		assert task.getVerification() instanceof SkillVerification;
		SkillVerification verif = (SkillVerification) task.getVerification();

		long totalAchieved = verif.getExperience().entrySet().stream()
				.filter(entry -> entry.getKey() != null)
				.filter(entry -> client.getSkillExperience(entry.getKey()) > entry.getValue())
				.count();

		return totalAchieved >= verif.getCount();
	}

	public Map<Skill, Integer> verificationData() {
		return Arrays.stream(Skill.values())
			.collect(Collectors.toMap(
				skill -> skill,
				skill -> client.getSkillExperience(skill)
			));
	}
}
