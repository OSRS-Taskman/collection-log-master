package com.collectionlogmaster.ui.neww;

import java.util.Arrays;
import lombok.NonNull;
import net.runelite.api.FontTypeFace;
import net.runelite.api.widgets.Widget;
import net.runelite.client.util.LinkBrowser;

public class UIUtil {
	private static final String WIDGET_NAME_FORMAT = "<col=ff9040>%s</col>";
	private static final String BASE_OSRS_WIKI_URL = "https://oldschool.runescape.wiki/w/%s";

	public static void openWikiLink(String itemName) {
		String wikiUrl = String.format(BASE_OSRS_WIKI_URL, itemName.replace(" ", "_"));
		LinkBrowser.browse(wikiUrl);
	}

	public static String formatName(String name) {
		return String.format(WIDGET_NAME_FORMAT, name);
	}

	public static int getTextHeight(@NonNull String text, @NonNull FontTypeFace font, int lineHeight, int maxWidth) {
		return lineHeight * getTextLineCount(text, font, maxWidth);
	}

	public static int getTextHeight(@NonNull String text, @NonNull FontTypeFace font, int maxWidth) {
		return getTextHeight(text, font, font.getBaseline(), maxWidth);
	}

	public static int getTextHeight(@NonNull String text, @NonNull Widget widget) {
		return getTextHeight(text, widget.getFont(), widget.getWidth());
	}

	public static int getTextLineCount(@NonNull String text, @NonNull FontTypeFace font, int maxWidth) {
		int spaceWidth = font.getTextWidth(" ");
		int[] wordWidths = Arrays.stream(text.split(" "))
				.mapToInt(font::getTextWidth)
				.toArray();

		int lineCount = 1;
		// account for first word not having a space before it
		int lineWidth = -spaceWidth;
		for (int wordWidth : wordWidths) {
			lineWidth += wordWidth + spaceWidth;

			if (lineWidth > maxWidth) {
				lineCount++;
				// include overflow word into next line
				lineWidth = wordWidth;
			}
		}

		return lineCount;
	}
}
