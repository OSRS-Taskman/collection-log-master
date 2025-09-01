package com.collectionlogmaster.ui.generic.button;

import static java.util.Map.entry;

import java.util.Map;
import net.runelite.api.FontID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetPositionMode;
import net.runelite.api.widgets.WidgetSizeMode;
import net.runelite.api.widgets.WidgetTextAlignment;
import net.runelite.api.widgets.WidgetType;
import org.intellij.lang.annotations.MagicConstant;

public class UISimpleButton extends UIButton<UISimpleButton> {
	private static final int BASE_GAP = 4;

	private Map<State, Integer> textColorTheme = Map.ofEntries(
			entry(State.DEFAULT, 0xCCCCCC),
			entry(State.HOVER, 0xFFFFFF),
			entry(State.DISABLED, 0x969696)
	);

	private Map<State, Integer> iconSpriteTheme = Map.ofEntries(
			entry(State.DEFAULT, -1),
			entry(State.HOVER, -1),
			entry(State.DISABLED, -1)
	);

	public static UISimpleButton createInside(Widget window) {
		return new UISimpleButton(window.createChild(WidgetType.LAYER));
	}

	private final Widget icon;
	private final Widget text;

	protected UISimpleButton(Widget widget) {
		super(widget);

		icon = widget.createChild(WidgetType.GRAPHIC);
		text = widget.createChild(WidgetType.TEXT);

		applyStaticStyles();
	}

	protected int getTextColor() {
		return textColorTheme.getOrDefault(getState(), 0xFFFFFF);
	}

	protected int getIconSprite() {
		return iconSpriteTheme.getOrDefault(getState(), -1);
	}

	public UISimpleButton setFontId(@MagicConstant(valuesFromClass = FontID.class) int font) {
		text.setFontId(font);
		return this;
	}

	public UISimpleButton setText(String textContent) {
		text.setText(textContent);
		return this;
	}

	public UISimpleButton setTextShadowed(boolean shadowed) {
		text.setTextShadowed(shadowed);
		return this;
	}

	public UISimpleButton setIconSpriteTheme(int defaultSprite, int hoverSprite, int disabledSprite) {
		iconSpriteTheme = Map.ofEntries(
				entry(State.DEFAULT, defaultSprite),
				entry(State.HOVER, hoverSprite),
				entry(State.DISABLED, disabledSprite)
		);

		return this;
	}

	public UISimpleButton setTextColorTheme(int defaultColor, int hoverColor, int disabledColor) {
		textColorTheme = Map.ofEntries(
				entry(State.DEFAULT, defaultColor),
				entry(State.HOVER, hoverColor),
				entry(State.DISABLED, disabledColor)
		);

		return this;
	}

	public UISimpleButton setIconSize(int width, int height) {
		icon.setSize(width, height);
		return this;
	}

	protected void applyStaticStyles() {
		widget.revalidate();

		icon.setXPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
				.setYPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
				.setOriginalY(0);

		text.setXPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
				.setYPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
				.setOriginalY(1)
				.setWidthMode(WidgetSizeMode.MINUS)
				.setHeightMode(WidgetSizeMode.MINUS)
				.setSize(0, 0)
				.setFontId(FontID.PLAIN_12)
				.setXTextAlignment(WidgetTextAlignment.CENTER)
				.setYTextAlignment(WidgetTextAlignment.CENTER)
				.revalidate();
	}

	@Override
	public void revalidate() {
		super.revalidate();

		// remove gap when one of the widgets is not used
		int effectiveGap = text.getText().isBlank() || getIconSprite() == -1 ? 0 : BASE_GAP;

		text.setOriginalWidth(text.getFont().getTextWidth(text.getText()))
				.setOriginalX((icon.getOriginalWidth() + effectiveGap) / 2)
				.setTextColor(getTextColor())
				.revalidate();

		icon.setOriginalX((text.getOriginalWidth() + effectiveGap) / -2)
				.setSpriteId(getIconSprite())
				.revalidate();
	}
}
