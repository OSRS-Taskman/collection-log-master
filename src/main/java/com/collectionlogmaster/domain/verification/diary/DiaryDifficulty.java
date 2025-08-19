package com.collectionlogmaster.domain.verification.diary;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@RequiredArgsConstructor
public enum DiaryDifficulty {
    EASY(2),
    MEDIUM(5),
    HARD(8),
    ELITE(11);

    private final int stackOffset;
}
