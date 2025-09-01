package com.collectionlogmaster.ui.generic;

import lombok.Getter;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetPositionMode;
import net.runelite.api.widgets.WidgetSizeMode;
import net.runelite.api.widgets.WidgetType;
import org.intellij.lang.annotations.MagicConstant;

// TODO: implement UIMultiSpriteButton using UIBorderContainer + UIButton
public class UIBorderedContainer extends UIComponent<UIBorderedContainer> {
	private final Widget background;
	private final Widget topLeftCorner;
	private final Widget topRightCorner;
	private final Widget bottomLeftCorner;
	private final Widget bottomRightCorner;
	private final Widget leftEdge;
	private final Widget topEdge;
	private final Widget rightEdge;
	private final Widget bottomEdge;

	@Getter
	private final Widget content;

	private BorderTheme theme = BorderTheme.NULL;

	public static UIBorderedContainer createInside(Widget window) {
		return new UIBorderedContainer(window.createChild(WidgetType.LAYER), WidgetType.LAYER);
	}

	public static UIBorderedContainer createInside(
		Widget window,
		@MagicConstant(valuesFromClass = WidgetType.class) int contentType
	) {
		return new UIBorderedContainer(window.createChild(WidgetType.LAYER), contentType);
	}

	public UIBorderedContainer(Widget widget, @MagicConstant(valuesFromClass = WidgetType.class) int contentType) {
		super(widget, WidgetType.LAYER);

		background = widget.createChild(WidgetType.GRAPHIC);
		topLeftCorner = widget.createChild(WidgetType.GRAPHIC);
		topRightCorner = widget.createChild(WidgetType.GRAPHIC);
		bottomLeftCorner = widget.createChild(WidgetType.GRAPHIC);
		bottomRightCorner = widget.createChild(WidgetType.GRAPHIC);
		leftEdge = widget.createChild(WidgetType.GRAPHIC);
		topEdge = widget.createChild(WidgetType.GRAPHIC);
		rightEdge = widget.createChild(WidgetType.GRAPHIC);
		bottomEdge = widget.createChild(WidgetType.GRAPHIC);
		content = widget.createChild(contentType);

		initializeWidgets();
	}

	public UIBorderedContainer setTheme(BorderTheme theme) {
		this.theme = theme;
		return this;
	}

	private void initializeWidgets() {
		background.setWidthMode(WidgetSizeMode.MINUS)
			.setHeightMode(WidgetSizeMode.MINUS)
			.setSpriteTiling(true)
			.revalidate();

		topLeftCorner.setPos(0, 0)
			.revalidate();

		topRightCorner.setXPositionMode(WidgetPositionMode.ABSOLUTE_RIGHT)
			.setPos(0, 0)
			.revalidate();

		bottomLeftCorner.setYPositionMode(WidgetPositionMode.ABSOLUTE_BOTTOM)
			.setPos(0, 0)
			.revalidate();

		bottomRightCorner.setXPositionMode(WidgetPositionMode.ABSOLUTE_RIGHT)
			.setYPositionMode(WidgetPositionMode.ABSOLUTE_BOTTOM)
			.setPos(0, 0)
			.revalidate();

		leftEdge.setYPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
			.setPos(0, 0)
			.setHeightMode(WidgetSizeMode.MINUS)
			.setSpriteTiling(true)
			.revalidate();

		topEdge.setXPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
			.setPos(0, 0)
			.setWidthMode(WidgetSizeMode.MINUS)
			.setSpriteTiling(true)
			.revalidate();

		rightEdge.setYPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
			.setXPositionMode(WidgetPositionMode.ABSOLUTE_RIGHT)
			.setPos(0, 0)
			.setHeightMode(WidgetSizeMode.MINUS)
			.setSpriteTiling(true)
			.revalidate();

		bottomEdge.setYPositionMode(WidgetPositionMode.ABSOLUTE_BOTTOM)
			.setXPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
			.setPos(0, 0)
			.setWidthMode(WidgetSizeMode.MINUS)
			.setSpriteTiling(true)
			.revalidate();
	}

	@Override
	public void revalidate() {
		int cornerSize = theme.getCornerSize();
		int edgeSize = theme.getEdgeSize();

		widget.revalidate();

		background.setPos(edgeSize, edgeSize)
			.setSize(edgeSize * 2, edgeSize * 2)
			.setSpriteId(theme.getBackground())
			.revalidate();

		topLeftCorner.setSize(cornerSize, cornerSize)
			.setSpriteId(theme.getTopLeftCorner())
			.revalidate();

		topRightCorner.setSize(cornerSize, cornerSize)
			.setSpriteId(theme.getTopRightCorner())
			.revalidate();

		bottomLeftCorner.setSize(cornerSize, cornerSize)
			.setSpriteId(theme.getBottomLeftCorner())
			.revalidate();

		bottomRightCorner.setSize(cornerSize, cornerSize)
			.setSpriteId(theme.getBottomRightCorner())
			.revalidate();

		leftEdge.setSize(edgeSize, cornerSize * 2)
			.setSpriteId(theme.getLeftEdge())
			.revalidate();

		topEdge.setSize(cornerSize * 2, edgeSize)
			.setSpriteId(theme.getTopEdge())
			.revalidate();

		rightEdge.setSize(edgeSize, cornerSize * 2)
			.setSpriteId(theme.getRightEdge())
			.revalidate();

		bottomEdge.setSize(cornerSize * 2, edgeSize)
			.setSpriteId(theme.getBottomEdge())
			.revalidate();

		content.revalidate();
	}
}