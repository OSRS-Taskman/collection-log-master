package com.collectionlogmaster.ui.neww.button;

import static java.util.Map.entry;

import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.gameval.SpriteID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetPositionMode;
import net.runelite.api.widgets.WidgetSizeMode;
import net.runelite.api.widgets.WidgetType;

public abstract class UIMultiSpriteButton<This extends UIMultiSpriteButton<This>> extends UIButton<This> {
	public static final int CORNER_SIZE = 9;

	private static final int[] DEFAULT_SPRITES = {
			SpriteID.TRADEBACKING,
			SpriteID.V2StoneButtonOut.A_TOP_LEFT,
			SpriteID.V2StoneButtonOut.A_TOP_RIGHT,
			SpriteID.V2StoneButtonOut.A_BOTTOM_LEFT,
			SpriteID.V2StoneButtonOut.A_BOTTOM_RIGHT,
			SpriteID.V2StoneButtonOut.A_MAP_EDGE_LEFT,
			SpriteID.V2StoneButtonOut.A_MAP_EDGE_TOP,
			SpriteID.V2StoneButtonOut.A_MAP_EDGE_RIGHT,
			SpriteID.V2StoneButtonOut.A_MAP_EDGE_BOTTOM,
	};

	private static final int[] HOVER_SPRITES = {
			SpriteID.TRADEBACKING_DARK,
			SpriteID.V2StoneButtonIn.A_TOP_LEFT,
			SpriteID.V2StoneButtonIn.A_TOP_RIGHT,
			SpriteID.V2StoneButtonIn.A_BOTTOM_LEFT,
			SpriteID.V2StoneButtonIn.A_BOTTOM_RIGHT,
			SpriteID.V2StoneButtonIn.A_LEFT,
			SpriteID.V2StoneButtonIn.A_TOP,
			SpriteID.V2StoneButtonIn.A_RIGHT,
			SpriteID.V2StoneButtonIn.A_BOTTOM,
	};

	private static final int[] DISABLED_SPRITES = {
			SpriteID.TRADEBACKING_DARK,
			SpriteID.V2StoneButton.TOP_LEFT,
			SpriteID.V2StoneButton.TOP_RIGHT,
			SpriteID.V2StoneButton.BOTTOM_LEFT,
			SpriteID.V2StoneButton.BOTTOM_RIGHT,
			SpriteID.V2StoneButton.LEFT,
			SpriteID.V2StoneButton.TOP,
			SpriteID.V2StoneButton.RIGHT,
			SpriteID.V2StoneButton.BOTTOM,
	};

	@Getter
	@RequiredArgsConstructor
	public enum StateTheme {
		DEFAULT(0xFFFFFF, DEFAULT_SPRITES),
		HOVER(0xFFFFFF, HOVER_SPRITES),
		DISABLED(0x969696, DISABLED_SPRITES);

		private final int textColor;
		private final int[] sprites;
	}

	private static final Map<State, StateTheme> THEME_MAP = Map.ofEntries(
			entry(State.DEFAULT, StateTheme.DEFAULT),
			entry(State.HOVER, StateTheme.HOVER),
			entry(State.DISABLED, StateTheme.DISABLED)
	);

	private final Widget background;
	private final Widget topLeftCorner;
	private final Widget topRightCorner;
	private final Widget bottomLeftCorner;
	private final Widget bottomRightCorner;
	private final Widget leftEdge;
	private final Widget topEdge;
	private final Widget rightEdge;
	private final Widget bottomEdge;
	protected Widget content;

	private final Widget[] allGraphics;

	protected UIMultiSpriteButton(Widget widget) {
		super(widget);

		background = widget.createChild(WidgetType.GRAPHIC);
		topLeftCorner = widget.createChild(WidgetType.GRAPHIC);
		topRightCorner = widget.createChild(WidgetType.GRAPHIC);
		bottomLeftCorner = widget.createChild(WidgetType.GRAPHIC);
		bottomRightCorner = widget.createChild(WidgetType.GRAPHIC);
		leftEdge = widget.createChild(WidgetType.GRAPHIC);
		topEdge = widget.createChild(WidgetType.GRAPHIC);
		rightEdge = widget.createChild(WidgetType.GRAPHIC);
		bottomEdge = widget.createChild(WidgetType.GRAPHIC);
		allGraphics = new Widget[] {
				background,
				topLeftCorner, topRightCorner, bottomLeftCorner, bottomRightCorner,
				leftEdge, topEdge, rightEdge, bottomEdge
		};

		createContent(widget);

		applyStaticStyles();
	}

	protected abstract void createContent(Widget widget);

	protected StateTheme getTheme() {
		return THEME_MAP.get(getState());
	}

	protected void applyStaticStyles() {
		widget.revalidate();

		background.setPos(0, 0)
				.setWidthMode(WidgetSizeMode.MINUS)
				.setHeightMode(WidgetSizeMode.MINUS)
				.setSize(0, 0)
				.setSpriteTiling(true)
				.revalidate();

		topLeftCorner.setPos(0, 0)
				.setSize(CORNER_SIZE, CORNER_SIZE)
				.revalidate();

		topRightCorner.setXPositionMode(WidgetPositionMode.ABSOLUTE_RIGHT)
				.setPos(0, 0)
				.setSize(CORNER_SIZE, CORNER_SIZE)
				.revalidate();

		bottomLeftCorner.setYPositionMode(WidgetPositionMode.ABSOLUTE_BOTTOM)
				.setPos(0, 0)
				.setSize(CORNER_SIZE, CORNER_SIZE)
				.revalidate();

		bottomRightCorner.setXPositionMode(WidgetPositionMode.ABSOLUTE_RIGHT)
				.setYPositionMode(WidgetPositionMode.ABSOLUTE_BOTTOM)
				.setPos(0, 0)
				.setSize(CORNER_SIZE, CORNER_SIZE)
				.revalidate();

		leftEdge.setYPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
				.setPos(0, 0)
				.setHeightMode(WidgetSizeMode.MINUS)
				.setSize(CORNER_SIZE, CORNER_SIZE * 2)
				.setSpriteTiling(true)
				.revalidate();

		topEdge.setXPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
				.setPos(0, 0)
				.setWidthMode(WidgetSizeMode.MINUS)
				.setSize(CORNER_SIZE * 2, CORNER_SIZE)
				.setSpriteTiling(true)
				.revalidate();

		rightEdge.setYPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
				.setXPositionMode(WidgetPositionMode.ABSOLUTE_RIGHT)
				.setPos(0, 0)
				.setHeightMode(WidgetSizeMode.MINUS)
				.setSize(CORNER_SIZE, CORNER_SIZE * 2)
				.setSpriteTiling(true)
				.revalidate();

		bottomEdge.setYPositionMode(WidgetPositionMode.ABSOLUTE_BOTTOM)
				.setXPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
				.setPos(0, 0)
				.setWidthMode(WidgetSizeMode.MINUS)
				.setSize(CORNER_SIZE * 2, CORNER_SIZE)
				.setSpriteTiling(true)
				.revalidate();
	}

	@Override
	public void revalidate() {
		super.revalidate();

		int[] sprites = getTheme().getSprites();
		for (int i = 0; i < allGraphics.length; i++) {
			allGraphics[i].setSpriteId(sprites[i])
					.revalidate();
		}

		content.revalidate();
	}
}
