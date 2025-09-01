package com.collectionlogmaster.ui;

import lombok.Getter;

@Getter
public enum SpriteOverride implements net.runelite.client.game.SpriteOverride {
	// we'll override these later by flipping the original sprite on the fly
	TALL_TABS_CORNER_VFLIP("transparent.png"),
	TALL_TABS_CORNER_HOVER_VFLIP("transparent.png");

	// we put `lastSpriteId` into a nested static class to force the
	// JVM into initializing it before calling the enum constructor
	private static class Memory {
		private static int lastSpriteId = -20000;
	}

	SpriteOverride(String fileName) {
		// we don't really care what the ID is, as long as it's
		// not repeated and we can reference it from the enum
		this.spriteId = --Memory.lastSpriteId;
		this.fileName = fileName;
	}

	private final int spriteId;

	private final String fileName;
}
