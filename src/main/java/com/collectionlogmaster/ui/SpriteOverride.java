package com.collectionlogmaster.ui;

import lombok.Getter;

@Getter
public enum SpriteOverride implements net.runelite.client.game.SpriteOverride {
    CURRENT_TASK("currentTask.png"),
    TAB_DASHBOARD("tab-dashboard.png"),
    TAB_DASHBOARD_HOVER("tab-dashboard-hover.png"),
    DIVIDER("divider.png"),
    TASK_LONG("task-long.png"),
    TASK_LONG_COMPLETE("task-long-complete.png"),
    UP_ARROW("up-arrow.png"),
    DOWN_ARROW("down-arrow.png"),
    TASK_LONG_CURRENT("task-long-current.png"),
    EASY_TAB("easy-tab.png"),
    EASY_TAB_HOVER("easy-tab-hover.png"),
    MEDIUM_TAB("medium-tab.png"),
    MEDIUM_TAB_HOVER("medium-tab-hover.png"),
    HARD_TAB("hard-tab.png"),
    HARD_TAB_HOVER("hard-tab-hover.png"),
    ELITE_TAB("elite-tab.png"),
    ELITE_TAB_HOVER("elite-tab-hover.png"),
    MASTER_TAB("master-tab.png"),
    MASTER_TAB_HOVER("master-tab-hover.png"),
    PAGE_UP_ARROW("page-up-arrow.png"),
    PAGE_DOWN_ARROW("page-down-arrow.png"),
    THUMB_TOP("thumb-top.png"),
    THUMB_MIDDLE("thumb-middle.png"),
    THUMB_BOTTOM("thumb-bottom.png"),
    TRANSPARENT("transparent.png"),

    // we'll override these later by flipping the original sprite on the fly
    TALL_TABS_CORNER_VFLIP("transparent.png"),
    TALL_TABS_CORNER_HOVER_VFLIP("transparent.png");

    // we put `lastSpriteId` into a nested static class to force the
    // JVM into initializing it before calling the enum constructor
    private static class Memory {
        private static int lastSpriteId = -20000;
    }

    SpriteOverride(String fileName) {
        // we don't really care what the ID is, as long as it's
        // not repeated and we can reference it from the enum
        this.spriteId = --Memory.lastSpriteId;
        this.fileName = fileName;
    }

    private final int spriteId;

    private final String fileName;
}
