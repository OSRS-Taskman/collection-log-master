package com.logmaster.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.logmaster.domain.adapters.EnumAdapter;
import com.logmaster.domain.adapters.VerificationAdapter;
import com.logmaster.domain.verification.Verification;
import com.logmaster.domain.verification.VerificationMethod;
import com.logmaster.domain.verification.diary.DiaryDifficulty;
import com.logmaster.domain.verification.diary.DiaryRegion;
import net.runelite.api.Skill;
import net.runelite.http.api.gson.ColorTypeAdapter;
import net.runelite.http.api.gson.InstantTypeAdapter;

import java.awt.*;
import java.time.Instant;

public class GsonOverride {
    /**
     * Custom Gson instance capable of parsing additional types.
     */
    public static final Gson GSON;

    static {
        GsonBuilder gsonBuilder = net.runelite.http.api.RuneLiteAPI.GSON.newBuilder()
                .registerTypeAdapter(Verification.class, new VerificationAdapter())
                .registerTypeAdapter(VerificationMethod.class, new EnumAdapter<>(VerificationMethod.class))
                .registerTypeAdapter(DiaryRegion.class, new EnumAdapter<>(DiaryRegion.class))
                .registerTypeAdapter(DiaryDifficulty.class, new EnumAdapter<>(DiaryDifficulty.class))
                .registerTypeAdapter(Skill.class, new EnumAdapter<>(Skill.class));

        GSON = gsonBuilder.create();
    }
}
