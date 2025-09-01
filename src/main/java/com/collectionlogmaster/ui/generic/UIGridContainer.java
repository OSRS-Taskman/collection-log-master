package com.collectionlogmaster.ui.generic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;
import org.intellij.lang.annotations.MagicConstant;

public class UIGridContainer extends UIComponent<UIGridContainer> {
	/**
	 * Initial distance between grid items. Final gap will
	 * be increased to accommodate for remaining space.
	 */
	private static final int BASE_GAP = 2;

	private final List<Widget> items = new ArrayList<>();

	public static UIGridContainer createInside(Widget window) {
		return new UIGridContainer(window.createChild(WidgetType.LAYER));
	}

	public UIGridContainer(Widget widget) {
		super(widget, WidgetType.LAYER);
	}

	public List<Widget> getItems() {
		return Collections.unmodifiableList(items);
	}

	public Widget createItem(@MagicConstant(valuesFromClass = WidgetType.class) int widgetType) {
		Widget newItem = widget.createChild(widgetType);
		items.add(newItem);
		return newItem;
	}

	private void repositionItems() {
		// we assume all items are same size
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
		int lastLineXOffset = Math.floorMod(-items.size(), maxItemsPerLine) * itemWidth / 2;

		int i = 0;
		for (Widget item : items) {
			int x = i % maxItemsPerLine;
			int y = i / maxItemsPerLine;
			int extraXOffset = y == lastLineIndex ? lastLineXOffset : 0;

			int posX = x * itemWidth + gridOffset + extraXOffset;
			int posY = y * itemHeight + BASE_GAP;

			item.setPos(posX, posY)
				.revalidate();

			i++;
		}
	}

	@Override
	public void revalidate() {
		widget.revalidate();

		if (items.isEmpty()) {
			return;
		}

		repositionItems();

		Widget lastItem = items.get(items.size() - 1);
		widget.setOriginalHeight(lastItem.getRelativeY() + lastItem.getHeight() + BASE_GAP);
	}
}
