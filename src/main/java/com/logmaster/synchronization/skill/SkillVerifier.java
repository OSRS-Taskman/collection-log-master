package com.logmaster.synchronization.skill;

import com.logmaster.domain.Task;
import com.logmaster.domain.verification.skill.SkillVerification;
import com.logmaster.synchronization.Verifier;
import lombok.NonNull;
import net.runelite.api.Client;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SkillVerifier implements Verifier {
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
}
