package com.logmaster.ui.generic;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.FontID;
import net.runelite.api.ScriptEvent;
import net.runelite.api.gameval.SpriteID;
import net.runelite.api.widgets.*;

import java.util.Set;

public class UINativeButton extends UIComponent {
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
	public enum State {
		DEFAULT(0xFFFFFF, DEFAULT_SPRITES),
		HOVER(0xFFFFFF, HOVER_SPRITES),
		DISABLED(0x969696, DISABLED_SPRITES);

		private final int textColor;
		private final int[] sprites;
	}

	@Getter
	private State state = State.DEFAULT;

	private final Widget background;
	private final Widget topLeftCorner;
	private final Widget topRightCorner;
	private final Widget bottomLeftCorner;
	private final Widget bottomRightCorner;
	private final Widget leftEdge;
	private final Widget topEdge;
	private final Widget rightEdge;
	private final Widget bottomEdge;
	private final Widget textWidget;

	private final Widget[] allGraphics;

	public UINativeButton(Widget widget) {
		super(widget, Set.of(WidgetType.LAYER));

		background = widget.createChild(WidgetType.GRAPHIC);
		topLeftCorner = widget.createChild(WidgetType.GRAPHIC);
		topRightCorner = widget.createChild(WidgetType.GRAPHIC);
		bottomLeftCorner = widget.createChild(WidgetType.GRAPHIC);
		bottomRightCorner = widget.createChild(WidgetType.GRAPHIC);
		leftEdge = widget.createChild(WidgetType.GRAPHIC);
		topEdge = widget.createChild(WidgetType.GRAPHIC);
		rightEdge = widget.createChild(WidgetType.GRAPHIC);
		bottomEdge = widget.createChild(WidgetType.GRAPHIC);
		allGraphics = new Widget[]{background, topLeftCorner, topRightCorner, bottomLeftCorner, bottomRightCorner, leftEdge, topEdge, rightEdge, bottomEdge};

		textWidget = widget.createChild(WidgetType.TEXT);

		applyStaticStyles();
		applyStatefulStyles();
	}

	public void setState(State state) {
		this.state = state;
		applyStatefulStyles();
	}

	public void setText(String text) {
		textWidget.setText(text);
	}

	@Override
	protected void onActionSelected(ScriptEvent e) {
		if (state == State.DISABLED) return;

		super.onActionSelected(e);
	}

	@Override
	protected void onMouseHover(ScriptEvent e) {
		super.onMouseHover(e);

		if (state == State.DEFAULT) {
			setState(State.HOVER);
		}
	}

	@Override
	protected void onMouseLeave(ScriptEvent e) {
		super.onMouseLeave(e);

		if (state == State.HOVER) {
			setState(State.DEFAULT);
		}
	}

	private void applyStaticStyles() {
		background
			.setPos(0, 0)
			.setWidthMode(WidgetSizeMode.MINUS)
			.setHeightMode(WidgetSizeMode.MINUS)
			.setSize(0, 0)
			.setSpriteTiling(true);

		topLeftCorner
			.setPos(0, 0)
			.setSize(CORNER_SIZE, CORNER_SIZE);

		topRightCorner
			.setXPositionMode(WidgetPositionMode.ABSOLUTE_RIGHT)
			.setPos(0, 0)
			.setSize(CORNER_SIZE, CORNER_SIZE);

		bottomLeftCorner
			.setYPositionMode(WidgetPositionMode.ABSOLUTE_BOTTOM)
			.setPos(0, 0)
			.setSize(CORNER_SIZE, CORNER_SIZE);

		bottomRightCorner
			.setXPositionMode(WidgetPositionMode.ABSOLUTE_RIGHT)
			.setYPositionMode(WidgetPositionMode.ABSOLUTE_BOTTOM)
			.setPos(0, 0)
			.setSize(CORNER_SIZE, CORNER_SIZE);

		leftEdge
			.setYPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
			.setPos(0, 0)
			.setHeightMode(WidgetSizeMode.MINUS)
			.setSize(CORNER_SIZE, CORNER_SIZE * 2)
			.setSpriteTiling(true);

		topEdge
			.setXPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
			.setPos(0, 0)
			.setWidthMode(WidgetSizeMode.MINUS)
			.setSize(CORNER_SIZE * 2, CORNER_SIZE)
			.setSpriteTiling(true);

		rightEdge
			.setYPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
			.setXPositionMode(WidgetPositionMode.ABSOLUTE_RIGHT)
			.setPos(0, 0)
			.setHeightMode(WidgetSizeMode.MINUS)
			.setSize(CORNER_SIZE, CORNER_SIZE * 2)
			.setSpriteTiling(true);

		bottomEdge
			.setYPositionMode(WidgetPositionMode.ABSOLUTE_BOTTOM)
			.setXPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
			.setPos(0, 0)
			.setWidthMode(WidgetSizeMode.MINUS)
			.setSize(CORNER_SIZE * 2, CORNER_SIZE)
			.setSpriteTiling(true);

		textWidget
			.setPos(0, 0)
			.setWidthMode(WidgetSizeMode.MINUS)
			.setHeightMode(WidgetSizeMode.MINUS)
			.setSize(0, 0)
			.setXTextAlignment(WidgetTextAlignment.CENTER)
			.setYTextAlignment(WidgetTextAlignment.CENTER)
			.setFontId(FontID.BOLD_12)
			.setTextShadowed(true);

		revalidate();
	}

	private void applyStatefulStyles() {
		int[] sprites = state.getSprites();
		for (int i = 0; i < allGraphics.length; i++) {
			allGraphics[i].setSpriteId(sprites[i]);
		}

		textWidget.setTextColor(state.getTextColor());
	}

	@Override
	public void revalidate() {
		super.revalidate();

		for (Widget g : allGraphics) {
			g.revalidate();
		}

		textWidget.revalidate();
	}
}
