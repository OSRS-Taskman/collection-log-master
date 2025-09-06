package com.collectionlogmaster.domain.verification.diary;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@RequiredArgsConstructor
public enum DiaryDifficulty {
	EASY(0),
	MEDIUM(3),
	HARD(6),
	ELITE(9);

	private final int stackOffset;
}
