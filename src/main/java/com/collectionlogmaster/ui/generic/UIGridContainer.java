package com.collectionlogmaster.ui.generic;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;
import org.intellij.lang.annotations.MagicConstant;

public class UIGridContainer extends UIComponent<UIGridContainer> {
	private static final int BASE_GAP = 2;

	// the LinkedHashSet is required to retain insertion order
	private final LinkedHashSet<Widget> items = new LinkedHashSet<>();

	public static UIGridContainer createInside(Widget window) {
		return new UIGridContainer(window.createChild(WidgetType.LAYER));
	}

	public UIGridContainer(Widget widget) {
		super(widget, WidgetType.LAYER);
	}

	public Set<Widget> getItems() {
		return Collections.unmodifiableSet(items);
	}

	public UIGridContainer clearItems() {
		widget.deleteAllChildren();
		items.clear();
		return this;
	}

	public Widget createItem(@MagicConstant(valuesFromClass = WidgetType.class) int widgetType) {
		Widget newItem = widget.createChild(widgetType);
		items.add(newItem);
		return newItem;
	}

	@Override
	public void revalidate() {
		widget.revalidate();

		if (items.isEmpty()) return;

		// we assume all items are square and same size
		Widget anyItem = items.iterator().next();
		int itemGap = BASE_GAP;
		int itemWidth = anyItem.getWidth() + itemGap;
		int itemHeight = anyItem.getHeight() + itemGap;
		int widthAvailable = widget.getWidth() - (BASE_GAP * 2);

		// redistribute remaining space first equally between items
		int spaceRemaining = widthAvailable % itemWidth;
		int maxItemsPerLine = widthAvailable / itemWidth;
		itemGap += spaceRemaining / maxItemsPerLine;
		itemWidth = anyItem.getWidth() + itemGap;

		// redistribute rest of remaining space as "padding" on the grid itself
		spaceRemaining = widthAvailable % itemWidth;
		int gridOffset = BASE_GAP + (spaceRemaining / 2) + (itemGap / 2);

		// the last line needs to be offset to remain centered
		int lastLineIndex = ((items.size() - 1) / maxItemsPerLine);
		int lastLineYOffset = Math.floorMod(-items.size(), maxItemsPerLine) * itemWidth / 2;

		int i = 0;
		for (Widget w : items) {
			int x = i % maxItemsPerLine;
			int y = i / maxItemsPerLine;
			int extraYOffset = y == lastLineIndex ? lastLineYOffset : 0;

			int posX = x * itemWidth + gridOffset + extraYOffset;
			int posY = y * itemHeight + BASE_GAP;

			w.setPos(posX, posY)
					.revalidate();

			i++;
		}

		widget.setOriginalHeight((lastLineIndex + 1) * itemHeight + BASE_GAP);
	}
}
