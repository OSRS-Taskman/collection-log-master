package com.collectionlogmaster.ui;

import static com.collectionlogmaster.CollectionLogMasterConfig.CONFIG_GROUP;

import com.collectionlogmaster.CollectionLogMasterConfig;
import com.collectionlogmaster.CollectionLogMasterPlugin;
import com.collectionlogmaster.domain.Task;
import com.collectionlogmaster.domain.TaskTier;
import com.collectionlogmaster.synchronization.SyncService;
import com.collectionlogmaster.synchronization.clog.CollectionLogService;
import com.collectionlogmaster.task.TaskService;
import com.collectionlogmaster.ui.component.MenuManager;
import com.collectionlogmaster.ui.component.TabManager;
import com.collectionlogmaster.ui.component.TaskDashboard;
import com.collectionlogmaster.ui.component.TaskList;
import com.collectionlogmaster.ui.state.StateChanged;
import com.collectionlogmaster.ui.state.StateStore;
import com.collectionlogmaster.util.EventBusSubscriber;
import com.collectionlogmaster.util.ImageUtil;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.SoundEffectID;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.SpriteID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.PluginMessage;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.input.MouseListener;
import net.runelite.client.input.MouseManager;
import net.runelite.client.input.MouseWheelListener;

@Slf4j
@Singleton
public class InterfaceManager extends EventBusSubscriber implements MouseListener, MouseWheelListener {
    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private CollectionLogMasterConfig config;

    @Inject
    private CollectionLogMasterPlugin plugin;

	@Inject
	private MouseManager mouseManager;

    @Inject
    private SpriteManager spriteManager;

    @Inject
    private CollectionLogService collectionLogService;

    @Inject
    private SyncService syncService;

    @Inject
    private TaskService taskService;

    @Inject
    private MenuManager burgerMenuManager;

    @Inject
    private StateStore stateStore;

    public TaskDashboard taskDashboard;
    private TaskList taskList;
    private TabManager tabManager;

    public void startUp() {
        super.startUp();
        mouseManager.registerMouseListener(this);
        mouseManager.registerMouseWheelListener(this);
        burgerMenuManager.startUp();

        this.spriteManager.addSpriteOverrides(SpriteOverride.values());
        overrideFlippedSprites();
    }

    public void shutDown() {
        super.shutDown();
        mouseManager.unregisterMouseListener(this);
        mouseManager.unregisterMouseWheelListener(this);
        burgerMenuManager.shutDown();
    }

    @Subscribe
    public void onStateChanged(StateChanged ev) {
        toggleTaskDashboard();
    }

	@Subscribe
	public void onConfigChanged(ConfigChanged e) {
        if (!e.getGroup().equals(CONFIG_GROUP)) {
			return;
		}

        if (!stateStore.isDashboardEnabled() || this.taskDashboard == null || this.tabManager == null) {
            return;
        }

        taskDashboard.updatePercentages();

        clientThread.invoke(tabManager::updateTabs);

        List<TaskTier> visibleTiers = taskService.getVisibleTiers();
        TaskTier activeTier = plugin.getSelectedTier();
        if (activeTier != null && !visibleTiers.contains(activeTier)) {
            clientThread.invoke(tabManager::activateTaskDashboard);
        }
    }

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded e) {
		if (e.getGroupId() != InterfaceID.COLLECTION) {
			return;
		}

        Widget window = client.getWidget(InterfaceID.Collection.CONTENT);
        if (window == null) return;

        createTaskDashboard(window);
        createTaskList(window);
        createTabManager(window);
        this.tabManager.setComponents(taskDashboard, taskList);

        this.tabManager.updateTabs();
        this.taskDashboard.setVisibility(false);
	}

	@Subscribe
	public void onWidgetClosed(WidgetClosed e) {
		if (e.getGroupId() != InterfaceID.COLLECTION) {
			return;
		}

        this.taskDashboard.setVisibility(false);
        this.taskList.setVisibility(false);
        tabManager.hideTabs();
	}

    Rectangle oldBounds;

	@Subscribe
	public void onGameTick(GameTick e) {
        Widget window = client.getWidget(621, 88);
        if (window == null) {
            oldBounds = null;
            return;
        }
        // Check if the window bounds have changed
        Rectangle newBounds = window.getBounds();
        if (oldBounds != null && oldBounds.equals(newBounds)) {
            return;
        }
        oldBounds = newBounds;

        if (this.taskList != null) {
            taskList.updateBounds();
        }
        if (this.taskDashboard != null) {
            taskDashboard.updateBounds();
        }
        if (this.tabManager != null) {
            tabManager.updateBounds();
        }
	}

    @Subscribe
    public void onPluginMessage(PluginMessage e) {
        if (!e.getNamespace().equals("resource-packs")) return;
        if (!e.getName().equals("pack-loaded")) return;

        overrideFlippedSprites();
    }

    private void overrideFlippedSprites() {
        // we can't use SpriteManager because it only accepts resource paths as an input
        client.getSpriteOverrides().put(
                SpriteOverride.TALL_TABS_CORNER_VFLIP.getSpriteId(),
                ImageUtil.getVFlippedSpritePixels(SpriteID.TabsTall._0, client)
        );
        client.getSpriteOverrides().put(
                SpriteOverride.TALL_TABS_CORNER_HOVER_VFLIP.getSpriteId(),
                ImageUtil.getVFlippedSpritePixels(SpriteID.TabsTall._1, client)
        );
    }

    public boolean isDashboardOpen() {
        return this.taskDashboard != null && this.taskDashboard.isVisible();
    }

    public void handleMouseWheel(MouseWheelEvent event) {
        if (this.taskList != null) {
            taskList.handleWheel(event);
        }
    }

    public void handleMousePress(int mouseX, int mouseY) {
        if (this.taskList != null && this.taskList.isVisible()) {
            taskList.handleMousePress(mouseX, mouseY);
        }
    }

    public void handleMouseDrag(int mouseX, int mouseY) {
        if (this.taskList != null && this.taskList.isVisible()) {
            taskList.handleMouseDrag(mouseX, mouseY);
        }
    }

    public void handleMouseRelease() {
        if (this.taskList != null) {
            taskList.handleMouseRelease();
        }
    }

    @Override
    public MouseWheelEvent mouseWheelMoved(MouseWheelEvent event) {
        handleMouseWheel(event);
        return event;
    }

    @Override
    public MouseEvent mouseClicked(MouseEvent event) {
        return event;
    }

    @Override
    public MouseEvent mousePressed(MouseEvent event) {
        handleMousePress(event.getX(), event.getY());
        return event;
    }

    @Override
    public MouseEvent mouseReleased(MouseEvent event) {
        handleMouseRelease();
        return event;
    }

    @Override
    public MouseEvent mouseDragged(MouseEvent event) {
        handleMouseDrag(event.getX(), event.getY());
        return event;
    }

    @Override
    public MouseEvent mouseMoved(MouseEvent event) {
        return event;
    }

    @Override
    public MouseEvent mouseEntered(MouseEvent event) {
        return event;
    }

    @Override
    public MouseEvent mouseExited(MouseEvent event) {
        return event;
    }

    private void createTabManager(Widget window) {
        this.tabManager = new TabManager(window, config, plugin);
    }

    private void createTaskDashboard(Widget window) {
        this.taskDashboard = new TaskDashboard(plugin, config, window, syncService, taskService, client);
        this.taskDashboard.setVisibility(false);
    }

    private void createTaskList(Widget window) {
        this.taskList = new TaskList(window, plugin, clientThread, config, collectionLogService, taskService);
        this.taskList.setVisibility(false);
    }

    private void toggleTaskDashboard() {
        if (this.taskDashboard == null) return;

        Task activeTask = taskService.getActiveTask();
        if (activeTask != null) {
            this.taskDashboard.setTask(activeTask, null);
        } else {
            this.taskDashboard.clearTask();
        }

        boolean enabled = stateStore.isDashboardEnabled();
        Widget contentWidget = client.getWidget(InterfaceID.Collection.CONTENT);
        if (contentWidget != null) {
            for (Widget c : contentWidget.getStaticChildren()) {
                // skip burger menu exit click area (temp fix)
                if (c.getId() == 40697874) continue;
                c.setHidden(enabled);
            }
        }
        Widget searchTitleWidget = client.getWidget(InterfaceID.Collection.SEARCH_TITLE);
        if (searchTitleWidget != null) {
            searchTitleWidget.setHidden(enabled);
        }

        if (enabled) {
            this.tabManager.activateTaskDashboard();
        } else {
            this.taskDashboard.setVisibility(false);
            this.taskList.setVisibility(false);
            this.tabManager.hideTabs();
        }

        // *Boop*
        this.client.playSoundEffect(SoundEffectID.UI_BOOP);
    }

    private boolean isTaskDashboardEnabled() {
        return stateStore.isDashboardEnabled();
    }

    public void completeTask() {
        boolean wasDashboardVisible = this.taskDashboard.isVisible();
        this.taskDashboard.updatePercentages();
        taskList.refreshTasks(0);
        // Restore previous visibility state
        this.taskDashboard.setVisibility(wasDashboardVisible);
        this.taskList.setVisibility(!wasDashboardVisible);
        this.tabManager.showTabs();
    }
}
