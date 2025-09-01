package com.collectionlogmaster.ui;

import com.collectionlogmaster.domain.Task;
import com.collectionlogmaster.ui.component.MainTabbedContainer;
import com.collectionlogmaster.ui.component.MenuManager;
import com.collectionlogmaster.ui.component.TaskInfo;
import com.collectionlogmaster.util.EventBusSubscriber;
import com.collectionlogmaster.util.ImageUtil;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.SpriteID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.SpriteManager;
import org.jetbrains.annotations.Nullable;

@Slf4j
@Singleton
public class InterfaceManager extends EventBusSubscriber {
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private SpriteManager spriteManager;

	@Inject
	private MenuManager menuManager;

	private MainTabbedContainer container = null;

	public void startUp() {
		super.startUp();
		menuManager.startUp();

		this.spriteManager.addSpriteOverrides(SpriteOverride.values());
		clientThread.invokeAtTickEnd(this::overrideFlippedSprites);
	}

	public void shutDown() {
		super.shutDown();
		menuManager.shutDown();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged e) {
		String configGroup = e.getGroup();
		if (configGroup.equals("resourcepacks")) {
			clientThread.invokeAtTickEnd(this::overrideFlippedSprites);
		}
	}

	public void hideCollectionLogContent(boolean hidden) {
		Widget window = getContentWidget();
		if (window == null) {
			return;
		}

		for (Widget w : window.getStaticChildren()) {
			w.setHidden(hidden)
				.revalidate();
		}
	}

	public void openMainContainer() {
		Widget content = getContentWidget();
		if (content == null) {
			return;
		}

		hideCollectionLogContent(true);

		container = MainTabbedContainer.createInside(content);
		container.revalidate();
	}

	public void closeMainContainer() {
		hideCollectionLogContent(false);

		if (container != null) {
			container.setHidden(true)
				.revalidate();
			container = null;
		}
	}

	public void openTaskInfo(Task task) {
		Widget content = getContentWidget();
		if (content == null) {
			return;
		}

		container.setHidden(true)
			.revalidate();

		TaskInfo.openInside(content, task)
			.thenAccept((r) -> {
				container.setHidden(false)
					.revalidate();
			});
	}

	private @Nullable Widget getContentWidget() {
		return client.getWidget(InterfaceID.Collection.CONTENT);
	}

	private void overrideFlippedSprites() {
		// we can't use SpriteManager because it only accepts resource paths as an input
		client.getSpriteOverrides().put(
			SpriteOverride.TALL_TABS_CORNER_VFLIP.getSpriteId(),
			ImageUtil.getVFlippedSpritePixels(SpriteID.TabsTall._2, client)
		);
		client.getSpriteOverrides().put(
			SpriteOverride.TALL_TABS_CORNER_HOVER_VFLIP.getSpriteId(),
			ImageUtil.getVFlippedSpritePixels(SpriteID.TabsTall._0, client)
		);
	}
}
