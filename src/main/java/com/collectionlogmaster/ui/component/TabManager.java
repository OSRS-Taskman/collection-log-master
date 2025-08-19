package com.collectionlogmaster.ui.component;

import com.collectionlogmaster.CollectionLogMasterConfig;
import com.collectionlogmaster.CollectionLogMasterPlugin;
import com.collectionlogmaster.domain.TaskTier;
import com.collectionlogmaster.ui.generic.UIButton;
import com.collectionlogmaster.ui.generic.UIGraphic;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;

import java.util.ArrayList;
import java.util.List;

import static com.collectionlogmaster.ui.InterfaceConstants.*;

public class TabManager {
    private final CollectionLogMasterConfig config;
    private final Widget window;
    private final CollectionLogMasterPlugin plugin;
    
    private List<UIButton> tabs;
    private UIButton taskDashboardTab;
    
    private TaskDashboard taskDashboard;
    private TaskList taskList;
    private UIGraphic divider;

    private int TAB_HEIGHT = 21;
    private int TAB_WIDTH = 66;
    private int DASHBOARD_TAB_WIDTH = 95;

    public TabManager(Widget window, CollectionLogMasterConfig config, CollectionLogMasterPlugin plugin) {
        this.window = window;
        this.config = config;
        this.plugin = plugin;
        
        createTabs();
        createDivider();
    }

    public void setComponents(TaskDashboard taskDashboard, TaskList taskList) {
        this.taskDashboard = taskDashboard;
        this.taskList = taskList;
    }

    private void createTabs() {
        // Remove any existing tabs from the window
        if (tabs != null) {
            for (UIButton tab : tabs) {
                if (tab != null && tab.getWidget() != null) {
                    tab.getWidget().setHidden(true);
                }
            }
        }
        tabs = new ArrayList<>();
        // Remove and recreate dashboard tab
        if (taskDashboardTab != null && taskDashboardTab.getWidget() != null) {
            taskDashboardTab.getWidget().setHidden(true);
        }
        Widget dashboardTabWidget = window.createChild(-1, WidgetType.GRAPHIC);
        taskDashboardTab = new UIButton(dashboardTabWidget);
        taskDashboardTab.setSprites(DASHBOARD_TAB_SPRITE_ID, DASHBOARD_TAB_HOVER_SPRITE_ID);
        taskDashboardTab.setSize(DASHBOARD_TAB_WIDTH, TAB_HEIGHT);
        taskDashboardTab.setPosition(10, 0);
        taskDashboardTab.addAction("View <col=ff9040>Dashboard</col>", this::activateTaskDashboard);
        taskDashboardTab.setVisibility(false);
        // Always create all tabs for all tiers
        for (TaskTier tier : TaskTier.values()) {
            Widget tabWidget = window.createChild(-1, WidgetType.GRAPHIC);
            UIButton tab = new UIButton(tabWidget);
            tab.setSize(TAB_WIDTH, TAB_HEIGHT);
            tab.setVisibility(false);
            tabs.add(tab);
        }
    }

    private void createDivider() {
        Widget dividerWidget = window.createChild(-1, WidgetType.GRAPHIC);
        divider = new UIGraphic(dividerWidget);
        divider.setSprite(DIVIDER_SPRITE_ID);
        divider.setSize(window.getWidth(), 1); // Full width minus margins
        divider.setPosition(0, 20);
        divider.revalidate();
    }

    public void updateBounds() {
        // Update divider width to match window width
        int windowWidth = window.getWidth();
        divider.setSize(windowWidth, 1);
        divider.revalidate();

        // Update tab positions
        updateTabPositions();
    }

    private void updateTabPositions() {
        int windowWidth = window.getWidth();
        int minSpacing = -30; // Allow up to 30px overlap
        // Count only visible tabs
        int visibleTierTabs = 0;
        for (TaskTier tier : TaskTier.values()) {
            if (tier.ordinal() >= config.hideBelow().ordinal()) {
                visibleTierTabs++;
            }
        }
        int totalTabsWidth = DASHBOARD_TAB_WIDTH + (visibleTierTabs * TAB_WIDTH);
        int spacing = (windowWidth - totalTabsWidth) / (visibleTierTabs + 2);
        // Allow negative spacing for overlap, but not less than minSpacing
        spacing = Math.max(minSpacing, Math.min(10, spacing));
        int dashboardX = spacing;
        taskDashboardTab.setPosition(dashboardX, 0);
        taskDashboardTab.getWidget().setPos(dashboardX, 0);
        int currentX = dashboardX + DASHBOARD_TAB_WIDTH + spacing;
        int tabIndex = 0;
        for (TaskTier tier : TaskTier.values()) {
            UIButton tab = tabs.get(tabIndex);
            if (tier.ordinal() >= config.hideBelow().ordinal()) {
                if (plugin.getSelectedTier() == tier && !this.taskDashboard.isVisible()) {
                    tab.setSprites(tier.tabSpriteHoverId);
                } else {
                    tab.setSprites(tier.tabSpriteId, tier.tabSpriteHoverId);
                }
                tab.setPosition(currentX, 0);
                tab.getWidget().setPos(currentX, 0);
                tab.setVisibility(true);
                tab.getWidget().setHidden(false);
                int finalTabIndex = tabIndex;
                tab.clearActions();
                tab.addAction(String.format("View <col=ff9040>%s Task List</col>", tier.displayName), () -> activateTaskListForTier(tier, finalTabIndex));
                currentX += TAB_WIDTH + spacing;
            } else {
                // Move out of view if not visible
                tab.setPosition(-1000, -1000);
                tab.getWidget().setPos(-1000, -1000);
                tab.setVisibility(false);
                tab.getWidget().setHidden(true);
            }
            tab.revalidate();
            tab.getWidget().revalidate();
            tabIndex++;
        }
        if (!this.taskDashboard.isVisible() && !this.taskList.isVisible()) {
            hideTabs();
        }
    }

    public void updateTabs() {
        hideTabs();
        if (tabs == null) return;
        if (!taskDashboard.isVisible() && !taskList.isVisible()) return;

        updateTabPositions();
        showTabs();
    }

    private void activateTaskListForTier(TaskTier tier, int tabIndex) {
        taskDashboardTab.setSprites(DASHBOARD_TAB_SPRITE_ID, DASHBOARD_TAB_HOVER_SPRITE_ID);
        if (plugin.getSelectedTier() != tier) {
            this.taskList.goToTop();
            plugin.setSelectedTier(tier);
        }
        updateTabs();
        tabs.get(tabIndex).setSprites(tier.tabSpriteHoverId);
        this.taskDashboard.setVisibility(false);
        this.taskList.refreshTasks(0);
        this.taskList.setVisibility(true);
    }

    public void activateTaskDashboard() {
        this.taskDashboardTab.setSprites(DASHBOARD_TAB_HOVER_SPRITE_ID);
        this.taskList.setVisibility(false);
        this.taskDashboard.setVisibility(true);
        updateTabs();
    }

    public void hideTabs() {
        if (this.taskDashboardTab != null) {
            this.taskDashboardTab.setVisibility(false);
        }
        if (this.tabs != null) {
            this.tabs.forEach(t -> t.setVisibility(false));
        }
    }

    public void showTabs() {
        // Hide tabs if neither list is visible
        if (!this.taskList.isVisible() && !this.taskDashboard.isVisible()) {
            this.hideTabs();
            return;
        }
        if (this.taskDashboardTab != null) {
            this.taskDashboardTab.setVisibility(true);
        }
        int tabIndex = 0;
        for (TaskTier tier : TaskTier.values()) {
            UIButton tab = this.tabs.get(tabIndex);
            if (tier.ordinal() >= config.hideBelow().ordinal()) {
                tab.setVisibility(true);
            } else {
                // Move out of view before hiding
                tab.setPosition(-1000, 0);
                tab.getWidget().setPos(-1000, 0);
                tab.setVisibility(false);
            }
            tabIndex++;
        }
        updateTabPositions();
    }
}
