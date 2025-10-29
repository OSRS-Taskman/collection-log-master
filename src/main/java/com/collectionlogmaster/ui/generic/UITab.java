package com.collectionlogmaster.ui.generic;

import static java.util.Map.entry;

import com.collectionlogmaster.ui.generic.button.UIButton;
import com.collectionlogmaster.ui.sprites.SpriteOverride;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.FontID;
import net.runelite.api.ScriptEvent;
import net.runelite.api.gameval.SpriteID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetPositionMode;
import net.runelite.api.widgets.WidgetSizeMode;
import net.runelite.api.widgets.WidgetTextAlignment;
import net.runelite.api.widgets.WidgetType;

public class UITab extends UIButton<UITab> {
	public static final int TAB_HEIGHT = 20;
	public static final int CORNER_SIZE = 20;
	public static final int PADDING_X = 15;
	public static final int TEXT_Y_OFFSET = 2;

	private static final int[] DEFAULT_SPRITES = {
		SpriteID.TabsTall._2,
		SpriteID.TabsTall._3,
		SpriteOverride.TALL_TABS_CORNER_VFLIP.getSpriteId()
	};

	private static final int[] HOVER_SPRITES = {
		SpriteID.TabsTall._0,
		SpriteID.TabsTall._1,
		SpriteOverride.TALL_TABS_CORNER_HOVER_VFLIP.getSpriteId()
	};

	private static final int[] DISABLED_SPRITES = HOVER_SPRITES;

	@Getter
	@RequiredArgsConstructor
	public enum StateTheme {
		DEFAULT(0xFFA82F, DEFAULT_SPRITES),
		HOVER(0xFFA82F, HOVER_SPRITES),
		DISABLED(0xFFA82F, DISABLED_SPRITES);

		private final int textColor;
		private final int[] sprites;
	}

	private static final Map<State, StateTheme> THEME_MAP = Map.ofEntries(
		entry(State.DEFAULT, StateTheme.DEFAULT),
		entry(State.HOVER, StateTheme.HOVER),
		entry(State.DISABLED, StateTheme.DISABLED)
	);

	private final Widget leftCorner;
	private final Widget middle;
	private final Widget rightCorner;
	private final Widget textWidget;

	public static UITab createInside(Widget window) {
		return new UITab(window.createChild(WidgetType.LAYER));
	}

	protected UITab(Widget widget) {
		super(widget);

		leftCorner = widget.createChild(WidgetType.GRAPHIC);
		middle = widget.createChild(WidgetType.GRAPHIC);
		rightCorner = widget.createChild(WidgetType.GRAPHIC);
		textWidget = widget.createChild(WidgetType.TEXT);

		initializeWidgets();
	}

	@Override
	protected void onActionSelected(ScriptEvent e) {
		super.onActionSelected(e);

		setState(State.DISABLED)
			.revalidate();
	}

	public UITab setText(String text) {
		textWidget.setText(text);
		return this;
	}

	protected StateTheme getTheme() {
		return THEME_MAP.get(getState());
	}

	protected void initializeWidgets() {
		widget.setOriginalHeight(CORNER_SIZE)
			.revalidate();

		leftCorner.setPos(0, 0)
			.setSize(CORNER_SIZE, CORNER_SIZE)
			.revalidate();

		middle.setXPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
			.setPos(0, 0)
			.setWidthMode(WidgetSizeMode.MINUS)
			.setHeightMode(WidgetSizeMode.MINUS)
			.setSize(CORNER_SIZE * 2, 0)
			.setSpriteTiling(true)
			.revalidate();

		rightCorner.setXPositionMode(WidgetPositionMode.ABSOLUTE_RIGHT)
			.setPos(0, 0)
			.setSize(CORNER_SIZE, CORNER_SIZE)
			.revalidate();

		textWidget.setPos(0, TEXT_Y_OFFSET)
			.setWidthMode(WidgetSizeMode.MINUS)
			.setHeightMode(WidgetSizeMode.MINUS)
			.setSize(0, 0)
			.setXTextAlignment(WidgetTextAlignment.CENTER)
			.setYTextAlignment(WidgetTextAlignment.CENTER)
			.setFontId(FontID.PLAIN_12)
			.revalidate();
	}

	@Override
	public void revalidate() {
		super.revalidate();

		int textWidth = textWidget.getFont().getTextWidth(textWidget.getText());
		widget.setOriginalWidth(textWidth + (PADDING_X * 2))
			.revalidate();

		StateTheme theme = getTheme();
		int[] themeSprites = theme.getSprites();
		leftCorner.setSpriteId(themeSprites[0])
			.revalidate();

		middle.setSpriteId(themeSprites[1])
			.revalidate();

		rightCorner.setSpriteId(themeSprites[2])
			.revalidate();

		textWidget.setTextColor(theme.getTextColor())
			.revalidate();
	}
}
