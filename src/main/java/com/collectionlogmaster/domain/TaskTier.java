package com.collectionlogmaster.domain;


public enum TaskTier {
	EASY("Easy"),
	MEDIUM("Medium"),
	HARD("Hard"),
	ELITE("Elite"),
	MASTER("Master");

	public final String displayName;

	TaskTier(String displayName) {
		this.displayName = displayName;
	}
}
