package com.collectionlogmaster.ui.generic;

import java.awt.Color;
import java.util.Arrays;
import lombok.NonNull;
import net.runelite.api.FontTypeFace;
import net.runelite.api.widgets.Widget;
import net.runelite.client.util.LinkBrowser;
import org.jetbrains.annotations.Range;

public class UIUtil {
	private static final String WIDGET_NAME_FORMAT = "<col=ff9040>%s</col>";
	private static final String BASE_OSRS_WIKI_URL = "https://oldschool.runescape.wiki/w/%s";
	public static final String FAQ_URL =
		"https://docs.google.com/document/d/e/2PACX-1vTHfXHzMQFbt_iYAP-O88uRhhz3wigh1KMiiuomU7ftli-rL_c3bRqfGYmUliE1EHcIr3LfMx2UTf2U/pub";

	public static void openWikiLink(String itemName) {
		String wikiUrl = String.format(BASE_OSRS_WIKI_URL, itemName.replace(" ", "_"));
		LinkBrowser.browse(wikiUrl);
	}

	public static void openFAQ() {
		LinkBrowser.browse(FAQ_URL);
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

	public static int getCompletionColor(@Range(from = 0, to = 1) float percent) {
		return Color.HSBtoRGB(percent / 3, 1, 1) & 0xFFFFFF;
	}

	public static int roundCompletionPercent(@Range(from = 0, to = 1) float percent) {
		int roundedPercent = (int) (percent * 100);

		// prevent showing 0% unless it's really 0
		if (roundedPercent == 0 && percent > 0) {
			return 1;
		}

		return roundedPercent;
	}
}
