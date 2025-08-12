package com.logmaster.ui.component;

import com.google.inject.Inject;
import com.logmaster.util.EventBusSubscriber;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.gameval.SpriteID;
import net.runelite.api.gameval.SpriteID.ClanRankIcons;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.eventbus.Subscribe;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class BurgerMenuManager extends EventBusSubscriber {
    private static final int DRAW_BURGER_MENU_SCRIPT_ID = 7812;
    private static final int COLLECTION_LOG_SETUP_SCRIPT_ID = 7797;
    private static final int COLLECTION_LOG_BURGER_MENU_WIDGET_ID = 40697929;

    private static final int BG_OPACITY = 255;
    private static final int BG_OPACITY_SELECTED = 230;
    private static final int TEXT_OPACITY = 0;
    private static final int TEXT_OPACITY_SELECTED = 200;
    private static final int TEXT_COLOR = 0xFF981F;
    private static final int TEXT_COLOR_SELECTED = 0xC8C8C8;
    private static final int TEXT_COLOR_HOVER = 0xFFFFFF;

    private static final String BUTTON_TEXT = "Tasks";
    private static final String ACTION_TEXT = "View Tasks Dashboard";

    @Inject
    private Client client;

    private Widget menu;
    private Widget ourBackground;
    private Widget ourText;
    private Widget firstBackground;
    private Widget firstText;

    private int baseMenuHeight = -1;

    @Getter
    private boolean selected = false;

    @Setter
    private Runnable onSelectChangedListener = null;

    @Subscribe
    public void onScriptPreFired(ScriptPreFired event) {
        int scriptId = event.getScriptId();
        if (scriptId == COLLECTION_LOG_SETUP_SCRIPT_ID) {
            setSelected(false);
            baseMenuHeight = -1;
        }

        if (scriptId != DRAW_BURGER_MENU_SCRIPT_ID) {
            return;
        }

        Object[] args = event.getScriptEvent().getArguments();
        int menuId = (int) args[3];
        if (menuId != COLLECTION_LOG_BURGER_MENU_WIDGET_ID) {
            return;
        }

        try {
            log.debug("Adding task dashboard button to menu with ID: {}", menuId);
            addButton(menuId);
        } catch (Exception e) {
            log.debug("Failed to add task dashboard button to menu: {}", e.getMessage());
        }
    }

    public void setSelected(boolean selected) {
        if (this.selected == selected) return;

        this.selected = selected;
        restyleOptions();

        if (this.onSelectChangedListener != null) {
            this.onSelectChangedListener.run();
        }
    }

    private void restyleOptions() {
        if (ourBackground == null || ourText == null) return;

        Widget selectedBackground = selected ? ourBackground : firstBackground;
        Widget selectedText = selected ? ourText : firstText;
        Widget defaultBackground = selected ? firstBackground : ourBackground;
        Widget defaultText = selected ? firstText : ourText;

        selectedBackground.setOpacity(BG_OPACITY_SELECTED);
        selectedText.setOpacity(TEXT_OPACITY_SELECTED)
            .setTextColor(TEXT_COLOR_SELECTED);

        defaultBackground.setOpacity(BG_OPACITY);
        defaultText.setOpacity(TEXT_OPACITY)
            .setTextColor(TEXT_COLOR);

        selectedBackground.revalidate();
        selectedText.revalidate();
        defaultBackground.revalidate();
        defaultText.revalidate();
    }

    private void addButton(int menuId) throws NullPointerException, NoSuchElementException {
//        extracted();


        Widget menu = Objects.requireNonNull(client.getWidget(menuId));
        List<Widget> menuChildren = Arrays.asList(Objects.requireNonNull(menu.getChildren()));
        if (baseMenuHeight == -1) {
            baseMenuHeight = menu.getOriginalHeight();
        }

        setupFirstWidgets(menuChildren);
        List<Widget> reversedMenuChildren = new ArrayList<>(menuChildren);
        Collections.reverse(reversedMenuChildren);
        Widget lastBackground = getFirstWidgetOfType(reversedMenuChildren, WidgetType.RECTANGLE);
        Widget lastText = getFirstWidgetOfType(reversedMenuChildren, WidgetType.TEXT);

        final int buttonHeight = lastBackground.getHeight();
        final int buttonY = lastBackground.getOriginalY() + buttonHeight;

        final boolean existingButton = menuChildren.stream()
                .anyMatch(w -> w.getText().equals(BUTTON_TEXT));

        if (!existingButton) {
            this.menu = menu;

            ourBackground = menu.createChild(WidgetType.RECTANGLE)
                    .setOriginalWidth(lastBackground.getOriginalWidth())
                    .setOriginalHeight(lastBackground.getOriginalHeight())
                    .setOriginalX(lastBackground.getOriginalX())
                    .setOriginalY(buttonY)
                    .setOpacity(lastBackground.getOpacity())
                    .setFilled(lastBackground.isFilled())
                    .setTextColor(lastBackground.getTextColor());
            ourBackground.revalidate();

            ourText = menu.createChild(WidgetType.TEXT)
                    .setText(BUTTON_TEXT)
                    .setTextColor(TEXT_COLOR)
                    .setFontId(lastText.getFontId())
                    .setTextShadowed(lastText.getTextShadowed())
                    .setOriginalWidth(lastText.getOriginalWidth())
                    .setOriginalHeight(lastText.getOriginalHeight())
                    .setOriginalX(lastText.getOriginalX())
                    .setOriginalY(buttonY)
                    .setXTextAlignment(lastText.getXTextAlignment())
                    .setYTextAlignment(lastText.getYTextAlignment());
            ourText.setHasListener(true);
            ourText.setOnMouseOverListener((JavaScriptCallback) ev -> { if (!selected) ourText.setTextColor(TEXT_COLOR_HOVER); });
            ourText.setOnMouseLeaveListener((JavaScriptCallback) ev -> { if (!selected) ourText.setTextColor(TEXT_COLOR); });
            ourText.setAction(0, ACTION_TEXT);
            ourText.setOnOpListener((JavaScriptCallback) ev -> {
                setSelected(true);
                hideMenu();
            });
            ourText.revalidate();
        }

        if (menu.getOriginalHeight() <= baseMenuHeight) {
            menu.setOriginalHeight((menu.getOriginalHeight() + buttonHeight));
        }

        restyleOptions();
        menu.revalidate();
        for (Widget child : menuChildren) {
            child.revalidate();
        }
    }

    private void extracted() {
        /*final*/
        int WIDGETS_PER_TAB = 11;
        /*final*/
        int ICON_WIDGET_OFFSET = 9;
        /*final*/
        int TEXT_WIDGET_OFFSET = 10;
        /*final*/
        int TEXT_COLOR_COMPLETE = 0x00FF00;
        /*final*/
        int TEXT_COLOR_CURRENT = 0xFF981F;
        /*final*/
        int TEXT_COLOR_FUTURE = 0xCCCCCC;

        Widget tierTabs = Objects.requireNonNull(client.getWidget(714, 18));
        Widget[] children = Objects.requireNonNull(tierTabs.getChildren());

        /*final*/
        int[] ICON_REPLACEMENT = { 2410, ClanRankIcons._65, ClanRankIcons._69, ClanRankIcons._72, ClanRankIcons._74, ClanRankIcons._73 };
        /*final*/
        String[] TEXT_REPLACEMENT = { "Dashboard", "Easy", "Medium", "Hard", "Elite", "Master" };
        /*final*/
        int[] COLOR_REPLACEMENT = { TEXT_COLOR_FUTURE, TEXT_COLOR_COMPLETE, TEXT_COLOR_CURRENT, TEXT_COLOR_FUTURE, TEXT_COLOR_FUTURE, TEXT_COLOR_FUTURE };
        for (int i = 0; i < ICON_REPLACEMENT.length; i++) {
            int baseIndex = i * WIDGETS_PER_TAB;

            int iconIndex = baseIndex + ICON_WIDGET_OFFSET;
            Widget iconWidget = children[iconIndex];
            iconWidget.setSpriteId(ICON_REPLACEMENT[i])
                .revalidate();

            int textIndex = baseIndex + TEXT_WIDGET_OFFSET;
            Widget textWidget = children[textIndex];
            textWidget.setText(TEXT_REPLACEMENT[i])
                .setTextColor(COLOR_REPLACEMENT[i])
                .revalidate();
        }

        for (int i = 1 * WIDGETS_PER_TAB; i < children.length; i++) {
            Widget child = children[i];

            child.setOriginalY(child.getOriginalY() + 10)
                .revalidate();
        }


        /*final*/
        int BAR_WIDGET_OFFSET = 1;
        /*final*/
        int FILL_WIDGET_OFFSET = 2;
        /*final*/
        int FIRST_SEP_WIDGET_OFFSET = 3;
        /*final*/
        int LAST_SEP_WIDGET_OFFSET = 7;
        /*final*/
        int PROGRESS_TEXT_WIDGET_OFFSET = 9;

        /*final*/
        String PROGRESS_TEXT = "100%                 98%                         0%                            0%                         0%";
        /*final*/
        int[] TIER_TOTAL_TASKS = { 163, 177, 209, 206, 175 };
        /*final*/
        int[] TIER_COMPLETE_TASKS = { 163, 175, 0, 0, 0 };
        /*final*/
        int TIER_TOTAL_SUM = Arrays.stream(TIER_TOTAL_TASKS).sum();
        /*final*/
        int TIER_COMPLETE_SUM = Arrays.stream(TIER_COMPLETE_TASKS).sum();

        Widget progressBar = Objects.requireNonNull(client.getWidget(714, 4));
        Widget[] pChildren = Objects.requireNonNull(progressBar.getChildren());

        Widget barWidget = pChildren[BAR_WIDGET_OFFSET];
        Widget fillWidget = pChildren[FILL_WIDGET_OFFSET];
        fillWidget.setOriginalWidth(barWidget.getOriginalWidth() * TIER_COMPLETE_SUM / TIER_TOTAL_SUM)
            .revalidate();

        int tierOffset = 0;
        for (int i = 0; i <= LAST_SEP_WIDGET_OFFSET - FIRST_SEP_WIDGET_OFFSET; i++) {
            Widget child = pChildren[FIRST_SEP_WIDGET_OFFSET + i];
            if (i >= TIER_TOTAL_TASKS.length - 1) {
                child.setHidden(true)
                        .revalidate();

                continue;
            }

            tierOffset += TIER_TOTAL_TASKS[i];
            child.setOriginalX(barWidget.getOriginalWidth() * tierOffset / TIER_TOTAL_SUM)
                    .revalidate();
        }

        Widget progTextWidget = pChildren[PROGRESS_TEXT_WIDGET_OFFSET];
        progTextWidget.setText(PROGRESS_TEXT).revalidate();
    }

    private void setupFirstWidgets(List<Widget> menuChildren) {
        firstBackground = getFirstWidgetOfType(menuChildren, WidgetType.RECTANGLE);
        firstText = getFirstWidgetOfType(menuChildren, WidgetType.TEXT);

        firstText.setHasListener(true);
        firstText.setOnMouseOverListener((JavaScriptCallback) ev -> { if (selected) firstText.setTextColor(TEXT_COLOR_HOVER); });
        firstText.setOnMouseLeaveListener((JavaScriptCallback) ev -> { if (selected) firstText.setTextColor(TEXT_COLOR); });
        firstText.setAction(0, firstText.getText());
        firstText.setOnOpListener((JavaScriptCallback) ev -> {
            setSelected(false);
            hideMenu();
        });
    }

    private void hideMenu() {
        if (menu != null) {
            menu.setHidden(true)
                .revalidate();
        }
    }

    private static Widget getFirstWidgetOfType(List<Widget> menuChildren, int widgetType) {
        return menuChildren.stream()
                .filter(w -> w.getType() == widgetType)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No widget of type" + widgetType + " found in menu"));
    }
}