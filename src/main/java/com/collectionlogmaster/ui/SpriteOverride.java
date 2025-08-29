package com.collectionlogmaster.ui;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SpriteOverride implements net.runelite.client.game.SpriteOverride {
    CURRENT_TASK(-20006, "currentTask.png"),
    TAB_DASHBOARD(-20007, "tab-dashboard.png"),
    TAB_DASHBOARD_HOVER(-20008, "tab-dashboard-hover.png"),
    DIVIDER(-20011, "divider.png"),
    TASK_LONG(-20012, "task-long.png"),
    TASK_LONG_COMPLETE(-20013, "task-long-complete.png"),
    UP_ARROW(-20014, "up-arrow.png"),
    DOWN_ARROW(-20015, "down-arrow.png"),
    TASK_LONG_CURRENT(-20016, "task-long-current.png"),
    EASY_TAB(-20017, "easy-tab.png"),
    EASY_TAB_HOVER(-20018, "easy-tab-hover.png"),
    MEDIUM_TAB(-20019, "medium-tab.png"),
    MEDIUM_TAB_HOVER(-20020, "medium-tab-hover.png"),
    HARD_TAB(-20021, "hard-tab.png"),
    HARD_TAB_HOVER(-20022, "hard-tab-hover.png"),
    ELITE_TAB(-20023, "elite-tab.png"),
    ELITE_TAB_HOVER(-20024, "elite-tab-hover.png"),
    MASTER_TAB(-20025, "master-tab.png"),
    MASTER_TAB_HOVER(-20026, "master-tab-hover.png"),
    PAGE_UP_ARROW(-20029, "page-up-arrow.png"),
    PAGE_DOWN_ARROW(-20030, "page-down-arrow.png"),
    THUMB_TOP(-20031, "thumb-top.png"),
    THUMB_MIDDLE(-20032, "thumb-middle.png"),
    THUMB_BOTTOM(-20033, "thumb-bottom.png"),
    TRANSPARENT(-20099, "transparent.png");

    private final int spriteId;

    private final String fileName;
}
