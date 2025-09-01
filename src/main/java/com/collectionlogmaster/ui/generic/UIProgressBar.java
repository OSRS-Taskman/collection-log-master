package com.collectionlogmaster.ui.generic;

import java.awt.Color;
import net.runelite.api.FontID;
import net.runelite.api.gameval.SpriteID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetSizeMode;
import net.runelite.api.widgets.WidgetTextAlignment;
import net.runelite.api.widgets.WidgetType;
import org.jetbrains.annotations.Range;

public class UIProgressBar extends UIComponent<UIProgressBar> {
	private final Widget border;
	private final Widget background;
	private final Widget barFill;
	private final Widget textWidget;

	public static UIProgressBar createInside(Widget window) {
		return new UIProgressBar(window.createChild(WidgetType.LAYER));
	}

	public UIProgressBar(Widget widget) {
		super(widget, WidgetType.LAYER);

		border = widget.createChild(WidgetType.RECTANGLE);
		background = widget.createChild(WidgetType.GRAPHIC);
		barFill = widget.createChild(WidgetType.GRAPHIC);
		textWidget = widget.createChild(WidgetType.TEXT);

		applyStaticStyles();
	}

	public UIProgressBar setPercent(@Range(from = 0, to = 1) float percent) {
		barFill.setOriginalWidth(Math.round((1 << 14) * percent))
				.revalidate();

		return this;
	}

	public UIProgressBar setText(String text) {
		textWidget.setText(text);

		return this;
	}

	private void applyStaticStyles() {
		widget.revalidate();

		border.setPos(0, 0)
				.setWidthMode(WidgetSizeMode.MINUS)
				.setHeightMode(WidgetSizeMode.MINUS)
				.setSize(0, 0)
				.setTextColor(0x242020)
				.revalidate();

		background.setPos(1, 1)
				.setWidthMode(WidgetSizeMode.MINUS)
				.setHeightMode(WidgetSizeMode.MINUS)
				.setSize(2, 2)
				.setSpriteTiling(true)
				.setSpriteId(SpriteID.CaProgressBar._1)
				.revalidate();

		barFill.setPos(1, 1)
				.setHeightMode(WidgetSizeMode.MINUS)
				.setWidthMode(WidgetSizeMode.ABSOLUTE_16384THS)
				.setOriginalHeight(2)
				.setSpriteTiling(true)
				.setSpriteId(SpriteID.CaProgressBar._0)
				.revalidate();

		// it just looks better if we put 2px lower
		textWidget.setPos(0, 2)
				.setWidthMode(WidgetSizeMode.MINUS)
				.setHeightMode(WidgetSizeMode.MINUS)
				.setSize(0, 2)
				.setXTextAlignment(WidgetTextAlignment.CENTER)
				.setYTextAlignment(WidgetTextAlignment.CENTER)
				.setFontId(FontID.PLAIN_12)
				.setTextColor(Color.WHITE.getRGB())
				.revalidate();
	}

	@Override
	public void revalidate() {
		super.revalidate();

		border.revalidate();
		background.revalidate();
		barFill.revalidate();
		textWidget.revalidate();
	}
}
