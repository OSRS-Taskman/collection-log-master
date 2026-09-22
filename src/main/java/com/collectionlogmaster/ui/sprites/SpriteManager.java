package com.collectionlogmaster.ui.sprites;

import com.collectionlogmaster.util.EventBusSubscriber;
import java.awt.Color;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NodeCache;
import net.runelite.api.SpritePixels;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;

@Slf4j
@Singleton
public class SpriteManager extends EventBusSubscriber {
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private net.runelite.client.game.SpriteManager spriteManager;

	private boolean isLoggedOut = false;

	public void startUp() {
		super.startUp();
		this.spriteManager.addSpriteOverrides(SpriteOverride.values());
		clientThread.invokeLater(this::overrideTransformedSprites);
	}

	@Subscribe(priority = Float.MIN_VALUE)
	public void onConfigChanged(ConfigChanged e) {
		String configGroup = e.getGroup();
		if (configGroup.equals("resourcepacks")) {
			clientThread.invokeLater(this::overrideTransformedSprites);
		}
	}

	@Subscribe(priority = Float.MIN_VALUE)
	public void onGameStateChanged(GameStateChanged e) {
		switch (e.getGameState()) {
			case LOGIN_SCREEN:
				isLoggedOut = true;
				break;

			case LOGGED_IN:
				if (isLoggedOut) {
					overrideTransformedSprites();
				}

				isLoggedOut = false;
				break;
		}
	}

	private boolean overrideTransformedSprites() {
		NodeCache spriteCache = client.getWidgetSpriteCache();

		// wait until cache is initialized
		if (spriteCache == null) {
			log.info("Skipping transformed sprite overrides until sprite cache is initialized");
			return false;
		}

		log.info("Applying transformed sprite overrides");

		spriteCache.reset();

		for (SpriteOverride spriteOverride : SpriteOverride.values()) {
			if (spriteOverride.getOriginalSpriteId() == null) {
				continue;
			}

			if (spriteOverride.getTransform() == SpriteOverride.Transform.VFLIP) {
				addSpriteOverride(
					spriteOverride.getSpriteId(),
					getVFlippedSpritePixels(spriteOverride.getOriginalSpriteId())
				);

				continue;
			}

			if (spriteOverride.getDye() != null) {
				addSpriteOverride(
					spriteOverride.getSpriteId(),
					getDyedSpritePixels(spriteOverride.getOriginalSpriteId(), spriteOverride.getDye())
				);
			}
		}

		return true;
	}

	private void addSpriteOverride(int spriteId, SpritePixels spritePixels) {
		// we can't use SpriteManager because it only accepts resource paths as input
		client.getSpriteOverrides().remove(spriteId);
		client.getSpriteOverrides().put(spriteId, spritePixels);
	}

	public SpritePixels getDyedSpritePixels(int spriteId, Color dye) {
		SpritePixels sp = getSpritePixels(spriteId);
		if (sp == null) {
			return null;
		}

		float dyeAlpha = dye.getAlpha() / 255f;
		int dyeRed = (int) (dye.getRed() * dyeAlpha);
		int dyeGreen = (int) (dye.getGreen() * dyeAlpha);
		int dyeBlue = (int) (dye.getBlue() * dyeAlpha);

		int[] originalPixels = sp.getPixels();
		int[] dyedPixels = new int[originalPixels.length];
		for (int i = 0; i < originalPixels.length; i++) {
			int originalPixel = originalPixels[i];

			// skip transparent pixels
			if (originalPixel == 0) {
				dyedPixels[i] = 0;
				continue;
			}

			Color originalColor = new Color(originalPixel);
			int finalRed = dyeRed + (int) (originalColor.getRed() * (1 - dyeAlpha));
			int finalGreen = dyeGreen + (int) (originalColor.getGreen() * (1 - dyeAlpha));
			int finalBlue = dyeBlue + (int) (originalColor.getBlue() * (1 - dyeAlpha));

			dyedPixels[i] = new Color(finalRed, finalGreen, finalBlue, 255 - originalColor.getAlpha()).getRGB();
		}

		return client.createSpritePixels(dyedPixels, sp.getWidth(), sp.getHeight());
	}

	public SpritePixels getVFlippedSpritePixels(int spriteId) {
		SpritePixels sp = getSpritePixels(spriteId);
		if (sp == null) {
			return null;
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

	private @Nullable SpritePixels getSpritePixels(int spriteId) {
		// we check overrides first so that in case another plugin (such as
		// resource packs) has overridden the sprite we still get the correct one
		SpritePixels sp = client.getSpriteOverrides().get(spriteId);
		if (sp != null) {
			return sp;
		}

		SpritePixels[] allSp = client.getSprites(client.getIndexSprites(), spriteId, 0);
		if (allSp == null || allSp.length < 1 || allSp[0] == null) {
			log.warn("Unable to find sprite for id {}", spriteId);
			return null;
		}

		return allSp[0];
	}
}
