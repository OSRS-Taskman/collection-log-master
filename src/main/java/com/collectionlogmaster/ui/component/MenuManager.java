package com.collectionlogmaster.ui.component;

import com.collectionlogmaster.ui.state.StateChanged;
import com.collectionlogmaster.ui.state.StateStore;
import com.google.inject.Inject;
import com.collectionlogmaster.util.EventBusSubscriber;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.eventbus.Subscribe;

import java.util.*;
import org.intellij.lang.annotations.MagicConstant;

/**
 * Responsible for handling the interaction with the hamburger menu in the top-left corner of the
 * collection log window. Heavily inspired by <a href="https://github.com/ReinhardtR/runeprofile-plugin/blob/master/src/main/java/com/runeprofile/ui/ManualUpdateButtonManager.java">RuneProfile's implementation</a>
 */
@Slf4j
public class MenuManager extends EventBusSubscriber {
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

    @Inject
    private StateStore stateStore;

    private Widget menu;
    private Widget ourBackground;
    private Widget ourText;
    private Widget firstBackground;
    private Widget firstText;

    private int baseMenuHeight = -1;

    @Subscribe
    public void onStateChanged(StateChanged ev) {
        restyleOptions();
    }

    @Subscribe
    public void onScriptPreFired(ScriptPreFired event) {
        int scriptId = event.getScriptId();
        if (scriptId == COLLECTION_LOG_SETUP_SCRIPT_ID) {
            stateStore.setDashboardEnabled(false);
            baseMenuHeight = -1;
            return;
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
            addButton(menuId);
        } catch (Exception e) {
            log.warn("Failed to add task dashboard button to menu: {}", e.getMessage());
        }
    }

    private void restyleOptions() {
        if (ourBackground == null || ourText == null) return;

        boolean selected = stateStore.isDashboardEnabled();
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
            ourText.setOnMouseOverListener((JavaScriptCallback) ev -> {
                if (!stateStore.isDashboardEnabled()) ourText.setTextColor(TEXT_COLOR_HOVER);
            });
            ourText.setOnMouseLeaveListener((JavaScriptCallback) ev -> {
                if (!stateStore.isDashboardEnabled()) ourText.setTextColor(TEXT_COLOR);
            });
            ourText.setAction(0, ACTION_TEXT);
            ourText.setOnOpListener((JavaScriptCallback) ev -> {
                stateStore.setDashboardEnabled(true);
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

    private void setupFirstWidgets(List<Widget> menuChildren) {
        firstBackground = getFirstWidgetOfType(menuChildren, WidgetType.RECTANGLE);
        firstText = getFirstWidgetOfType(menuChildren, WidgetType.TEXT);

        firstText.setHasListener(true);
        firstText.setOnMouseOverListener((JavaScriptCallback) ev -> {
            if (stateStore.isDashboardEnabled()) firstText.setTextColor(TEXT_COLOR_HOVER);
        });
        firstText.setOnMouseLeaveListener((JavaScriptCallback) ev -> {
            if (stateStore.isDashboardEnabled()) firstText.setTextColor(TEXT_COLOR);
        });
        firstText.setAction(0, firstText.getText());
        firstText.setOnOpListener((JavaScriptCallback) ev -> {
            stateStore.setDashboardEnabled(false);
            hideMenu();
        });
    }

    private void hideMenu() {
        if (menu != null) {
            menu.setHidden(true)
                .revalidate();
        }

        Widget burgerMenuOverlay = client.getWidget(40697874);
        if (burgerMenuOverlay != null) {
            burgerMenuOverlay.setHidden(true)
                    .revalidate();
        }
    }

    private static Widget getFirstWidgetOfType(List<Widget> menuChildren, @MagicConstant(valuesFromClass = WidgetType.class) int widgetType) {
        return menuChildren.stream()
                .filter(w -> w.getType() == widgetType)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No widget of type" + widgetType + " found in menu"));
    }
}