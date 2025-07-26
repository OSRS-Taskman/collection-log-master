package com.logmaster.domain.verification.skill;

import com.logmaster.domain.verification.Verification;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.runelite.api.Skill;

import java.util.Map;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SkillVerification extends Verification {
    private @NonNull Map<Skill, Integer> experience;
    private int count;
}
