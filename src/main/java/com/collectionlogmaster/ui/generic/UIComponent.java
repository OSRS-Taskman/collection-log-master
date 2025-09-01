package com.collectionlogmaster.ui.generic;

import lombok.Getter;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetPositionMode;
import net.runelite.api.widgets.WidgetSizeMode;
import net.runelite.api.widgets.WidgetType;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.Range;

/**
 * @param <This> The child class; this is e for the chaining to work properly
 */
@SuppressWarnings("unchecked")
public abstract class UIComponent<This extends UIComponent<This>> {
	@Getter
	protected Widget widget;

	protected UIComponent(Widget widget, @MagicConstant(valuesFromClass = WidgetType.class) int allowedType) {
		if (allowedType != widget.getType()) {
			String msg = String.format("Incompatible widget's type given; %d given, %d expected", allowedType, widget.getType());
			throw new RuntimeException(msg);
		}

		this.widget = widget;
	}

	public This setOpacity(@Range(from = 0, to = 255) int transparency) {
		widget.setOpacity(transparency);
		return (This) this;
	}

	public This setOriginalX(int originalX) {
		widget.setOriginalX(originalX);
		return (This) this;
	}

	public int getOriginalX() {
		return widget.getOriginalX();
	}

	public This setOriginalY(int originalY) {
		widget.setOriginalY(originalY);
		return (This) this;
	}

	public int getOriginalY() {
		return widget.getOriginalY();
	}

	public int getRelativeX() {
		return widget.getRelativeX();
	}

	public int getRelativeY() {
		return widget.getRelativeY();
	}

	public This setPos(int x, int y) {
		widget.setPos(x, y);
		return (This) this;
	}

	public This setOriginalHeight(int originalHeight) {
		widget.setOriginalHeight(originalHeight);
		return (This) this;
	}

	public int getOriginalHeight() {
		return widget.getOriginalHeight();
	}


	public This setOriginalWidth(int originalWidth) {
		widget.setOriginalWidth(originalWidth);
		return (This) this;
	}

	public int getOriginalWidth() {
		return widget.getOriginalWidth();
	}

	public int getHeight() {
		return widget.getHeight();
	}

	public int getWidth() {
		return widget.getWidth();
	}

	public This setSize(int width, int height) {
		widget.setSize(width, height);
		return (This) this;
	}

	public This setXPositionMode(@MagicConstant(valuesFromClass = WidgetPositionMode.class) int xpm) {
		widget.setXPositionMode(xpm);
		return (This) this;
	}

	public This setYPositionMode(@MagicConstant(valuesFromClass = WidgetPositionMode.class) int ypm) {
		widget.setYPositionMode(ypm);
		return (This) this;
	}

	public This setWidthMode(@MagicConstant(valuesFromClass = WidgetSizeMode.class) int widthMode) {
		widget.setWidthMode(widthMode);
		return (This) this;
	}

	public This setHeightMode(@MagicConstant(valuesFromClass = WidgetSizeMode.class) int heightMode) {
		widget.setHeightMode(heightMode);
		return (This) this;
	}

	public This setHidden(boolean hidden) {
		widget.setHidden(hidden);
		return (This) this;
	}

	public void revalidate() {
		widget.revalidate();
	}
}
