package com.collectionlogmaster.ui;

import com.collectionlogmaster.domain.Task;
import com.collectionlogmaster.ui.component.MainTabbedContainer;
import com.collectionlogmaster.ui.component.MenuManager;
import com.collectionlogmaster.ui.component.TaskInfo;
import com.collectionlogmaster.ui.sprites.SpriteManager;
import com.collectionlogmaster.ui.state.StateStore;
import com.collectionlogmaster.util.EventBusSubscriber;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.Subscribe;
import org.jetbrains.annotations.Nullable;

@Slf4j
@Singleton
public class InterfaceManager extends EventBusSubscriber {
	public static final int COLLECTION_LOG_SETUP_SCRIPT_ID = 7797;
	public static final int COLLECTION_LOG_OVERVIEW_SCRIPT_ID = 2388;

	@Inject
	private Client client;

	@Inject
	private SpriteManager spriteManager;

	@Inject
	private MenuManager menuManager;

	@Inject
	private StateStore stateStore;

	private MainTabbedContainer container = null;

	private TaskInfo taskInfo = null;

	public void startUp() {
		super.startUp();
		menuManager.startUp();
		spriteManager.startUp();
	}

	public void shutDown() {
		super.shutDown();
		menuManager.shutDown();
		spriteManager.shutDown();
	}

	@Subscribe
	public void onScriptPreFired(ScriptPreFired event) {
		int scriptId = event.getScriptId();
		if (scriptId != COLLECTION_LOG_SETUP_SCRIPT_ID && scriptId != COLLECTION_LOG_OVERVIEW_SCRIPT_ID) {
			return;
		}

		close();
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded e) {
		if (e.getGroupId() != InterfaceID.COLLECTION) {
			return;
		}

		close();
	}

	@Subscribe
	public void onWidgetClosed(WidgetClosed e) {
		if (e.getGroupId() != InterfaceID.COLLECTION) {
			return;
		}

		close();
	}

	public void hideCollectionLogContent(boolean hidden) {
		Widget window = getContentWidget();
		if (window == null) {
			return;
		}

		for (Widget w : window.getStaticChildren()) {
			if (w.getId() == InterfaceID.Collection.BURGER_MENU_OVERLAY) {
				continue;
			}

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

		if (container != null) {
			container.unregister();
		}

		container = MainTabbedContainer.createInside(content);
		container.revalidate();
	}

	public void openTaskInfo(Task task) {
		Widget content = getContentWidget();
		if (content == null) {
			return;
		}

		container.setHidden(true)
			.revalidate();

		taskInfo = TaskInfo.openInside(content, task);
		taskInfo.revalidate();
		taskInfo.getCloseFuture()
			.thenAccept((r) -> {
				taskInfo = null;
				container.setHidden(false)
					.revalidate();
			});
	}

	private @Nullable Widget getContentWidget() {
		return client.getWidget(InterfaceID.Collection.CONTENT);
	}

	public void close() {
		hideCollectionLogContent(false);

		if (container != null) {
			container.setHidden(true)
				.revalidate();
			container.unregister();
		}
		container = null;

		if (taskInfo != null) {
			taskInfo.close();
		}
		taskInfo = null;

		stateStore.setDashboardEnabled(false);
	}
}
