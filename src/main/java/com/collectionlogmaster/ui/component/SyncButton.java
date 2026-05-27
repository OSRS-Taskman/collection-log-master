package com.collectionlogmaster.ui.component;

import com.collectionlogmaster.CollectionLogMasterPlugin;
import com.collectionlogmaster.domain.Task;
import com.collectionlogmaster.synchronization.SyncService;
import com.collectionlogmaster.taskapp.migration.MigrationHelper;
import com.collectionlogmaster.ui.generic.button.UITextButton;
import java.awt.Color;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.chatbox.ChatboxPanelManager;
import net.runelite.client.util.ColorUtil;

@Slf4j
public class SyncButton extends UITextButton {
	public static final Color HIGHLIGHT_COLOR = new Color(0xFF981F);

	@Inject
	private SyncService syncService;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ChatboxPanelManager chatboxPanelManager;

	@Inject
	private MigrationHelper migrationHelper;

	@Setter
	private TaskDashboard taskDashboard;

	public static SyncButton createInside(Widget window) {
		return new SyncButton(window.createChild(WidgetType.LAYER));
	}

	protected SyncButton(Widget widget) {
		super(widget);
		CollectionLogMasterPlugin.getStaticInjector().injectMembers(this);
	}

	@Override
	protected void initializeWidgets() {
		super.initializeWidgets();

		this.setText("Sync")
			.setName("")
			.setAction("Sync", this::sync)
			.revalidate();
	}

	private void sync() {
		CompletableFuture<Boolean> panelFuture = new CompletableFuture<>();

		if (migrationHelper.canMigrate()) {
			Task oldActiveTask = migrationHelper.getOldActiveTask();

			String title = "<br>You have an old active task. Would you like to migrate it?"
				+ "<br>" + oldActiveTask.getName();

			chatboxPanelManager.openTextMenuInput(title)
				.option("1. Yes", () -> panelFuture.complete(true))
				.option("2. No", () -> panelFuture.complete(false))
				.build();
		} else {
			panelFuture.complete(false);
		}

		panelFuture.thenAccept(
			(Boolean shouldMigrate) -> {
				CompletableFuture<Void> migrationFuture = new CompletableFuture<>();
				if (shouldMigrate) {
					migrationFuture = migrationHelper.migrate();
				} else {
					migrationHelper.markAsMigrated();
					migrationFuture.complete(null);
				}

				migrationFuture.thenRun(() -> {
					// syncService.sync() has to run in the client thread for reading skills/diary data
					clientThread.invoke(
						() -> syncService.sync()
							.thenRun(() -> clientThread.invoke(() -> taskDashboard.revalidate()))
					);
				});
			}
		);
	}

	private String getDesyncedTooltip(List<Task> desyncedTasks) {
		if (desyncedTasks.isEmpty()) {
			return "";
		}

		String header = String.format("You have %d task(s) not synced to your collection log:", desyncedTasks.size());
		String footer = desyncedTasks.size() > 5 ? String.format("<br>and %d more", desyncedTasks.size() - 5) : "";
		String list = desyncedTasks.stream()
			.limit(5)
			.map(t -> String.format("<br>- %s", t.getName()))
			.collect(Collectors.joining());

		return ColorUtil.wrapWithColorTag(header, HIGHLIGHT_COLOR)
			+ list
			+ ColorUtil.wrapWithColorTag(footer, Color.LIGHT_GRAY);
	}

	private String getMigrationTooltip() {
		Task oldActiveTask = migrationHelper.getOldActiveTask();
		if (oldActiveTask == null) {
			return "";
		}

		String header = "You have a mismatching active task from your old plugin save data:";

		return ColorUtil.wrapWithColorTag(header, HIGHLIGHT_COLOR)
			+ "<br>- " + oldActiveTask.getName();
	}

	private String getFullTooltip(List<Task> desyncedTasks) {
		String migrationTooltip = getMigrationTooltip();
		String desyncedTooltip = getDesyncedTooltip(desyncedTasks);

		String separator = "";
		if (!migrationTooltip.isEmpty() && !desyncedTooltip.isEmpty()) {
			separator = "<br><br>";
		}

		return migrationTooltip + separator + desyncedTooltip;
	}

	@Override
	public void revalidate() {
		List<Task> desyncedTasks = syncService.check(true);

		if (desyncedTasks.isEmpty() && !migrationHelper.canMigrate()) {
			this.setText("Sync")
				.setState(State.DISABLED)
				.setTooltip("You're all synced up, nothing to do there!");
		} else {
			this.setText("Sync " + ColorUtil.wrapWithColorTag("(!)", HIGHLIGHT_COLOR))
				.setTooltip(getFullTooltip(desyncedTasks));

			if (getState() == State.DISABLED) {
				setState(State.DEFAULT);
			}
		}

		super.revalidate();
	}
}
