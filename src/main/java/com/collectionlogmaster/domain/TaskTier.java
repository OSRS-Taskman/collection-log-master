package com.collectionlogmaster.domain;

import com.collectionlogmaster.ui.SpriteOverride;

public enum TaskTier {
	EASY("Easy", SpriteOverride.EASY_TAB.getSpriteId(), SpriteOverride.EASY_TAB_HOVER.getSpriteId()),
	MEDIUM("Medium", SpriteOverride.MEDIUM_TAB.getSpriteId(), SpriteOverride.MEDIUM_TAB_HOVER.getSpriteId()),
	HARD("Hard", SpriteOverride.HARD_TAB.getSpriteId(), SpriteOverride.HARD_TAB_HOVER.getSpriteId()),
	ELITE("Elite", SpriteOverride.ELITE_TAB.getSpriteId(), SpriteOverride.ELITE_TAB_HOVER.getSpriteId()),
	MASTER("Master", SpriteOverride.MASTER_TAB.getSpriteId(), SpriteOverride.MASTER_TAB_HOVER.getSpriteId());

	public final String displayName;
	public final int tabSpriteId;
	public final int tabSpriteHoverId;

	TaskTier(String displayName, int tabSpriteId, int tabSpriteHoverId) {
		this.displayName = displayName;
		this.tabSpriteId = tabSpriteId;
		this.tabSpriteHoverId = tabSpriteHoverId;
	}
}
