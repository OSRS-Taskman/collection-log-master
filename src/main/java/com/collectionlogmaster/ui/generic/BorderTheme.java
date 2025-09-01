package com.collectionlogmaster.ui.generic;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.gameval.SpriteID;

@Getter
@RequiredArgsConstructor
public enum BorderTheme {
	NULL(0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1),
	ETCHED(
		8,
		4,
		SpriteID.SettingsTabs._16,
		SpriteID.TabsEtchedCorner._0,
		SpriteID.TabsEtchedCorner._1,
		SpriteID.TabsEtchedCorner._2,
		SpriteID.TabsEtchedCorner._3,
		SpriteID.SettingsTabs._14,
		SpriteID.SettingsTabs._12,
		SpriteID.SettingsTabs._15,
		SpriteID.SettingsTabs._13
	);

	private final int cornerSize;
	private final int edgeSize;
	private final int background;
	private final int topLeftCorner;
	private final int topRightCorner;
	private final int bottomLeftCorner;
	private final int bottomRightCorner;
	private final int leftEdge;
	private final int topEdge;
	private final int rightEdge;
	private final int bottomEdge;
}
