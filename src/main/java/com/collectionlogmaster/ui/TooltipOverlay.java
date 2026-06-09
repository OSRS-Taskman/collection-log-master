package com.collectionlogmaster.ui;

import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;

@Singleton
@Slf4j
public class TooltipOverlay extends Overlay {
	@Inject
	private TooltipManager tooltipManager;

	private Tooltip tooltip;

	public void setTooltip(String tooltipText) {
		tooltip = new Tooltip(tooltipText);
	}

	public void clearTooltip() {
		tooltip = null;
	}

	@Override
	public Dimension render(Graphics2D g) {
		if (tooltip != null) {
			tooltipManager.add(tooltip);
		}

		return null;
	}
}
