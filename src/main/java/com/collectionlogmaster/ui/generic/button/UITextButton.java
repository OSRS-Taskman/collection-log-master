package com.collectionlogmaster.ui.generic.button;

import net.runelite.api.FontID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetSizeMode;
import net.runelite.api.widgets.WidgetTextAlignment;
import net.runelite.api.widgets.WidgetType;
import org.intellij.lang.annotations.MagicConstant;

public class UITextButton extends UIMultiSpriteButton<UITextButton> {
	public static UITextButton createInside(Widget window) {
		return new UITextButton(window.createChild(WidgetType.LAYER));
	}

	public UITextButton(Widget widget) {
		super(widget);
	}

	@Override
	protected void createContent(Widget widget) {
		content = widget.createChild(WidgetType.TEXT);
	}

	@Override
	protected void applyStaticStyles() {
		super.applyStaticStyles();

		content.setPos(0, 0)
				.setWidthMode(WidgetSizeMode.MINUS)
				.setHeightMode(WidgetSizeMode.MINUS)
				.setSize(0, 0)
				.setXTextAlignment(WidgetTextAlignment.CENTER)
				.setYTextAlignment(WidgetTextAlignment.CENTER)
				.setFontId(FontID.BOLD_12)
				.setTextShadowed(true)
				.revalidate();
	}

	public UITextButton setFont(@MagicConstant(valuesFromClass = FontID.class) int font) {
		content.setFontId(font);
		return this;
	}

	public UITextButton setText(String text) {
		content.setText(text);
		return this;
	}

	@Override
	public void revalidate() {
		content.setTextColor(getTheme().getTextColor());

		super.revalidate();
	}
}
