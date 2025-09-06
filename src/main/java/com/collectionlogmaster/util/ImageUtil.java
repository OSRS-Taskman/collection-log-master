package com.collectionlogmaster.util;

import net.runelite.api.Client;
import net.runelite.api.SpritePixels;

public class ImageUtil extends net.runelite.client.util.ImageUtil {
	public static SpritePixels getVFlippedSpritePixels(int spriteId, Client client) {
		// we check overrides first so that in case another plugin (such as
		// resource packs) has overridden the sprite we still get the correct one
		SpritePixels sp = client.getSpriteOverrides().get(spriteId);

		if (sp == null) {
			SpritePixels[] allSp = client.getSprites(client.getIndexSprites(), spriteId, 0);
			if (allSp == null || allSp.length < 1 || allSp[0] == null) {
				return null;
			}

			sp = allSp[0];
		}

		int[] originalPixels = sp.getPixels();
		int[] flippedPixels = new int[originalPixels.length];
		for (int i = 0; i < sp.getHeight(); i++) {
			int baseOffset = i * sp.getWidth();
			for (int j = 0; j < sp.getWidth(); j++) {
				flippedPixels[baseOffset + j] = originalPixels[baseOffset + sp.getWidth() - j - 1];
			}
		}

		return client.createSpritePixels(flippedPixels, sp.getWidth(), sp.getHeight());
	}
}
