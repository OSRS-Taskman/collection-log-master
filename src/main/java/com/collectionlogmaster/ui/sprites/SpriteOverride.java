package com.collectionlogmaster.ui.sprites;

import java.awt.Color;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.NonNull;
import net.runelite.api.gameval.SpriteID;

@Getter
public enum SpriteOverride implements net.runelite.client.game.SpriteOverride {
	// we'll override these later by processing the original sprite on the fly
	TALL_TABS_CORNER_VFLIP(SpriteID.TabsTall._2, Transform.VFLIP),
	TALL_TABS_CORNER_HOVER_VFLIP(SpriteID.TabsTall._0, Transform.VFLIP),

	SETTINGS_TAB_BACKGROUND_GREEN_DYED(SpriteID.SettingsTabs._16, Dyes.GREEN),
	TABS_ETCHED_TOP_LEFT_CORNER_GREEN_DYED(SpriteID.TabsEtchedCorner._0, Dyes.GREEN),
	TABS_ETCHED_TOP_RIGHT_CORNER_GREEN_DYED(SpriteID.TabsEtchedCorner._1, Dyes.GREEN),
	TABS_ETCHED_BOTTOM_LEFT_CORNER_GREEN_DYED(SpriteID.TabsEtchedCorner._2, Dyes.GREEN),
	TABS_ETCHED_BOTTOM_RIGHT_CORNER_GREEN_DYED(SpriteID.TabsEtchedCorner._3, Dyes.GREEN),
	SETTINGS_TAB_LEFT_EDGE_GREEN_DYED(SpriteID.SettingsTabs._14, Dyes.GREEN),
	SETTINGS_TAB_TOP_EDGE_GREEN_DYED(SpriteID.SettingsTabs._12, Dyes.GREEN),
	SETTINGS_TAB_RIGHT_EDGE_GREEN_DYED(SpriteID.SettingsTabs._15, Dyes.GREEN),
	SETTINGS_TAB_BOTTOM_EDGE_GREEN_DYED(SpriteID.SettingsTabs._13, Dyes.GREEN),

	SETTINGS_TAB_BACKGROUND_GOLD_DYED(SpriteID.SettingsTabs._16, Dyes.GOLD),
	TABS_ETCHED_TOP_LEFT_CORNER_GOLD_DYED(SpriteID.TabsEtchedCorner._0, Dyes.GOLD),
	TABS_ETCHED_TOP_RIGHT_CORNER_GOLD_DYED(SpriteID.TabsEtchedCorner._1, Dyes.GOLD),
	TABS_ETCHED_BOTTOM_LEFT_CORNER_GOLD_DYED(SpriteID.TabsEtchedCorner._2, Dyes.GOLD),
	TABS_ETCHED_BOTTOM_RIGHT_CORNER_GOLD_DYED(SpriteID.TabsEtchedCorner._3, Dyes.GOLD),
	SETTINGS_TAB_LEFT_EDGE_GOLD_DYED(SpriteID.SettingsTabs._14, Dyes.GOLD),
	SETTINGS_TAB_TOP_EDGE_GOLD_DYED(SpriteID.SettingsTabs._12, Dyes.GOLD),
	SETTINGS_TAB_RIGHT_EDGE_GOLD_DYED(SpriteID.SettingsTabs._15, Dyes.GOLD),
	SETTINGS_TAB_BOTTOM_EDGE_GOLD_DYED(SpriteID.SettingsTabs._13, Dyes.GOLD);

	// we put `lastSpriteId` into a nested static class to force the
	// JVM into initializing it before calling the enum constructor
	private static class Memory {
		private static int lastSpriteId = -20000;
	}

	private static class Dyes {
		public static final Color GREEN = new Color(0, 255, 0, 25);
		public static final Color GOLD = new Color(255, 215, 0, 25);
	}

	public enum Transform {
		VFLIP;
	}

	private final int spriteId;

	private String fileName;

	private @Nullable Integer originalSpriteId = null;

	private @Nullable Color dye = null;
	private @Nullable Transform transform = null;

	SpriteOverride() {
		// we don't really care what the ID is, as long as it's
		// not repeated and we can reference it from the enum
		this.spriteId = --Memory.lastSpriteId;
		this.fileName = "transparent.png";
	}

	SpriteOverride(String fileName) {
		this();
		this.fileName = fileName;
	}

	SpriteOverride(int originalSpriteId, @NonNull Color dye) {
		this();
		this.originalSpriteId = originalSpriteId;
		this.dye = dye;
	}

	SpriteOverride(int originalSpriteId, @NonNull Transform transform) {
		this();
		this.originalSpriteId = originalSpriteId;
		this.transform = transform;
	}
}
