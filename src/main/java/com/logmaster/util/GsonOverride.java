package com.logmaster.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.logmaster.domain.Tag;
import com.logmaster.domain.adapters.EnumAdapter;
import com.logmaster.domain.adapters.VerificationAdapter;
import com.logmaster.domain.verification.Verification;
import com.logmaster.domain.verification.VerificationMethod;
import com.logmaster.domain.verification.diary.DiaryDifficulty;
import com.logmaster.domain.verification.diary.DiaryRegion;
import net.runelite.api.Skill;

import javax.inject.Inject;

public class GsonOverride {
    /**
     * Custom Gson instance capable of parsing additional types.
     */
    public static Gson GSON;

    @Inject
    public GsonOverride(Gson originalGson) {
        GsonBuilder gsonBuilder = originalGson.newBuilder()
                .registerTypeAdapter(Verification.class, new VerificationAdapter())
                .registerTypeAdapter(VerificationMethod.class, new EnumAdapter<>(VerificationMethod.class))
                .registerTypeAdapter(DiaryRegion.class, new EnumAdapter<>(DiaryRegion.class))
                .registerTypeAdapter(DiaryDifficulty.class, new EnumAdapter<>(DiaryDifficulty.class))
                .registerTypeAdapter(Skill.class, new EnumAdapter<>(Skill.class))
                .registerTypeAdapter(Tag.class, new EnumAdapter<>(Tag.class));

        GSON = gsonBuilder.create();
    }
}
