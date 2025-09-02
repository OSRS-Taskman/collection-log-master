package com.collectionlogmaster.ui.generic;

import java.awt.Rectangle;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetPositionMode;
import net.runelite.api.widgets.WidgetSizeMode;
import net.runelite.api.widgets.WidgetType;
import org.intellij.lang.annotations.MagicConstant;

/**
 * Wraps a given Widget into a class we can control and extend from, exposing most common Widget
 * methods. This should be used as the base for more advanced components, usually having a LAYER
 * widget as the base and more children widgets for extended functionality.
 *
 * @param <This> The class that is extending UIComponent; this
 *               is required for the chaining to work properly
 */
@Slf4j
public abstract class UIComponent<This extends UIComponent<This>> {
	@Getter
	protected Widget widget;

	protected UIComponent(Widget widget) {
		this(widget, WidgetType.LAYER);
	}

	protected UIComponent(Widget widget, @MagicConstant(valuesFromClass = WidgetType.class) int allowedType) {
		if (allowedType != widget.getType()) {
			String msg = String.format("Incompatible widget; %d given, %d expected", allowedType, widget.getType());
			throw new RuntimeException(msg);
		}

		this.widget = widget;
	}

	public This setOriginalX(int originalX) {
		widget.setOriginalX(originalX);
		return castThis();
	}

	public int getOriginalX() {
		return widget.getOriginalX();
	}

	public This setOriginalY(int originalY) {
		widget.setOriginalY(originalY);
		return castThis();
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
		return castThis();
	}

	public This setOriginalWidth(int originalWidth) {
		widget.setOriginalWidth(originalWidth);
		return castThis();
	}

	public int getOriginalWidth() {
		return widget.getOriginalWidth();
	}

	public This setOriginalHeight(int originalHeight) {
		widget.setOriginalHeight(originalHeight);
		return castThis();
	}

	public int getOriginalHeight() {
		return widget.getOriginalHeight();
	}

	public int getWidth() {
		return widget.getWidth();
	}

	public int getHeight() {
		return widget.getHeight();
	}

	public This setSize(int width, int height) {
		widget.setSize(width, height);
		return castThis();
	}

	public This setXPositionMode(@MagicConstant(valuesFromClass = WidgetPositionMode.class) int xpm) {
		widget.setXPositionMode(xpm);
		return castThis();
	}

	public This setYPositionMode(@MagicConstant(valuesFromClass = WidgetPositionMode.class) int ypm) {
		widget.setYPositionMode(ypm);
		return castThis();
	}

	public This setWidthMode(@MagicConstant(valuesFromClass = WidgetSizeMode.class) int widthMode) {
		widget.setWidthMode(widthMode);
		return castThis();
	}

	public This setHeightMode(@MagicConstant(valuesFromClass = WidgetSizeMode.class) int heightMode) {
		widget.setHeightMode(heightMode);
		return castThis();
	}

	public This setHidden(boolean hidden) {
		widget.setHidden(hidden);
		return castThis();
	}

	public Rectangle getBounds() {
		return widget.getBounds();
	}

	public boolean isHidden() {
		return widget.isHidden();
	}

	/**
	 * Cast this to the generic type given so that chaining methods preserve
	 * the child class type and its methods can still be called in the chain.
	 */
	@SuppressWarnings("unchecked")
	protected This castThis() {
		return (This) this;
	}

	/**
	 * Unlike Widget::revalidate, this method is also responsible
	 * for applying many stateful style changes.
	 */
	public void revalidate() {
		widget.revalidate();
	}
}
