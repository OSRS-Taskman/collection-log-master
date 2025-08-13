package com.logmaster.ui.component;

import com.logmaster.LogMasterPlugin;
import com.logmaster.domain.Task;
import com.logmaster.domain.verification.clog.CollectionLogVerification;
import com.logmaster.synchronization.clog.CollectionLogService;
import com.logmaster.task.TaskService;
import com.logmaster.ui.generic.UIGraphic;
import com.logmaster.ui.generic.UILabel;
import com.logmaster.ui.generic.UINativeButton;
import com.logmaster.ui.generic.UIPage;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.FontID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.util.LinkBrowser;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TaskInfo extends UIPage {
    private final static int OFFSET_X = 0;
    private final static int OFFSET_Y = 21;

    private final Widget window;
    private final LogMasterPlugin plugin;
    private final CollectionLogService collectionLogService;
    private final TaskService taskService;

    private final static int BUTTON_HEIGHT = 30;
    private final static int BUTTON_WIDTH = 68;
    private final static int LARGE_BUTTON_WIDTH = 140;

    private TaskDashboard taskDashboard;
    private TaskList taskList;
    private TabManager tabManager;
    private UIPage previousVisiblePage = null;
    private Task currentTask = null;

    private Rectangle bounds = new Rectangle();
    private int windowWidth = 480;
    private int windowHeight = 252;
    private int wrapperX = 0;
    private int wrapperY = 0;
    private int wrapperHeight = 231;
    private int windowX = 0;
    private int windowY = 0;

    public TaskInfo(Widget window, LogMasterPlugin plugin, CollectionLogService collectionLogService, TaskService taskService) {
        this.window = window;
        this.plugin = plugin;
        this.collectionLogService = collectionLogService;
        this.taskService = taskService;

        updateBounds();
    }

    public void setComponents(TaskDashboard taskDashboard, TaskList taskList, TabManager tabManager) {
        this.taskDashboard = taskDashboard;
        this.taskList = taskList;
        this.tabManager = tabManager;
    }

    @Override
    public void setVisibility(boolean visible) {
        super.setVisibility(visible);
        if (visible) {
            this.previousVisiblePage = this.taskDashboard.isVisible() ? this.taskDashboard : this.taskList;
            this.previousVisiblePage.setVisibility(false);
            this.tabManager.hideTabs();
        } else if (this.previousVisiblePage != null) {
            this.previousVisiblePage.setVisibility(true);
            if (this.taskList.isVisible()) {
                this.taskList.updateBounds();
            }
            this.tabManager.showTabs();
            this.tabManager.updateTabs();
            previousVisiblePage = null;
        }
    }

    private UILabel titleLabel;
    private UILabel tipLabel;
    private UINativeButton wikiBtn;
    private UINativeButton closeBtn;
    private UINativeButton completeBtn;
    private UILabel progressLabel;
    private UIGraphic progressBarBg;
    private UIGraphic progressBarFill;
    private List<UIGraphic> taskIcons = new ArrayList<>();
    private UILabel extraItemsLabel;

    public void showTask(String taskId) {
        currentTask = taskService.getTaskById(taskId);

        // Show the task title
        if (titleLabel == null) {
            titleLabel = new UILabel(window.createChild(-1, WidgetType.TEXT));
        }
        titleLabel.setFont(FontID.BOLD_12);
        titleLabel.setText(currentTask.getName());
        titleLabel.getWidget().setHidden(false);
        titleLabel.getWidget().setTextColor(Color.WHITE.getRGB());
        titleLabel.getWidget().setTextShadowed(true);
        titleLabel.getWidget().setName(currentTask.getName());
        this.add(titleLabel);

        // Show the task tip
        if (tipLabel == null) {
            tipLabel = new UILabel(window.createChild(-1, WidgetType.TEXT));
            this.add(tipLabel);
        }
        tipLabel.setFont(FontID.PLAIN_12);
        tipLabel.setText(currentTask.getTip());
        tipLabel.getWidget().setHidden(false);
        tipLabel.getWidget().setTextColor(Color.WHITE.getRGB());
        tipLabel.getWidget().setTextShadowed(true);
        tipLabel.getWidget().setName(currentTask.getName());

        int itemIndex = 0;
        if (currentTask.getVerification() instanceof CollectionLogVerification) {
            CollectionLogVerification verification = (CollectionLogVerification) currentTask.getVerification();
            int[] itemIds = verification.getItemIds();

            // Create or reuse a UIGraphic for the progress bar background
            if (progressBarBg == null) {
                progressBarBg = new UIGraphic(window.createChild(-1, WidgetType.RECTANGLE));
                this.add(progressBarBg);
            }
            progressBarBg.getWidget().setFilled(true);
            progressBarBg.getWidget().setOpacity(100);
            progressBarBg.getWidget().setTextColor(new Color(40, 40, 40).getRGB()); // dark background
            progressBarBg.getWidget().setBorderType(1);

            // Create or reuse a UIGraphic for the progress bar fill
            if (progressBarFill == null) {
                progressBarFill = new UIGraphic(window.createChild(-1, WidgetType.RECTANGLE));
                this.add(progressBarFill);
            }
            progressBarFill.getWidget().setFilled(true);
            progressBarFill.getWidget().setOpacity(100);
            progressBarFill.getWidget().setTextColor(new Color(60, 180, 75).getRGB()); // green fill
            progressBarFill.getWidget().setBorderType(0);

            // Progress label on top of the bar
            if (progressLabel == null) {
                progressLabel = new UILabel(window.createChild(-1, WidgetType.TEXT));
                this.add(progressLabel);
            }
            progressLabel.setFont(FontID.PLAIN_12);
            progressLabel.getWidget().setTextColor(Color.WHITE.getRGB());
            progressLabel.getWidget().setTextShadowed(true);
            progressLabel.getWidget().setName(currentTask.getName());

            if (itemIds != null && itemIds.length > 0) {
                int itemSize = 32;
                for (int itemId : itemIds) {
                    UIGraphic itemImage = itemIndex < taskIcons.size() ? taskIcons.get(itemIndex) : null;
                    if (itemImage == null) {
                        itemImage = new UIGraphic(window.createChild(-1, WidgetType.GRAPHIC));
                        taskIcons.add(itemImage);
                        this.add(itemImage);
                    }
                    itemImage.getWidget().setHidden(false);
                    itemImage.getWidget().setBorderType(1);
                    itemImage.setSize(itemSize, itemSize);
                    itemImage.setItem(itemId);
                    boolean isObtained = collectionLogService.isItemObtained(itemId);
                    itemImage.setOpacity(isObtained ? 1 : 0.3f);
                    String itemName = plugin.itemManager.getItemComposition(itemId).getName();
                    itemImage.clearActions();
                    itemImage.addAction(itemName, () -> {});
                    itemIndex++;
                }
            }
        }
        // Extra items label if needed
        if (extraItemsLabel == null) {
            extraItemsLabel = new UILabel(window.createChild(-1, WidgetType.TEXT));
            this.add(extraItemsLabel);
        }
        extraItemsLabel.setFont(FontID.PLAIN_11);
        extraItemsLabel.setText("");
        extraItemsLabel.getWidget().setTextColor(Color.WHITE.getRGB());
        extraItemsLabel.getWidget().setTextShadowed(true);
        extraItemsLabel.getWidget().setName(currentTask.getName());

        if (wikiBtn == null) {
            wikiBtn = new UINativeButton(window.createChild(WidgetType.LAYER));
            this.add(wikiBtn);
        }
        wikiBtn.setSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        wikiBtn.setText("Wiki");
        wikiBtn.clearActions();
        wikiBtn.addAction("View Wiki", () -> LinkBrowser.browse(currentTask.getWikiLink()));

        if (closeBtn == null) {
            closeBtn = new UINativeButton(window.createChild(WidgetType.LAYER));
            closeBtn.addAction("Close Task Info", this::closeTask);
            this.add(closeBtn);
        }
        closeBtn.setSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        closeBtn.setText("Back");

        if (completeBtn == null) {
            completeBtn = new UINativeButton(window.createChild(WidgetType.LAYER));
            this.add(completeBtn);
        }
        completeBtn.setSize(LARGE_BUTTON_WIDTH, BUTTON_HEIGHT);
        completeBtn.clearActions();
        if (taskService.isComplete(taskId)) {
            completeBtn.setText("Mark incomplete");
            completeBtn.addAction("Mark as <col=c0392b>incomplete</col>", () -> toggleTask(taskId));
        } else {
            completeBtn.setText("Mark complete");
            completeBtn.addAction("Mark as <col=27ae60>complete</col>", () -> toggleTask(taskId));
        }

        // Hide task list/dashboard/tabs, show task info
        this.setVisibility(true);
        this.setPositions();
    }

    private void toggleTask(String taskId) {
        completeBtn.clearActions();

        if (taskService.isComplete(taskId)) {
            taskService.uncomplete(taskId);
            completeBtn.setText("Mark complete");
            completeBtn.addAction("Mark as <col=27ae60>complete</col>", () -> toggleTask(taskId));
        } else {
            taskService.complete(taskId);
            completeBtn.setText("Mark incomplete");
            completeBtn.addAction("Mark as <col=c0392b>incomplete</col>", () -> toggleTask(taskId));
        }
    }

    private void setPositions() {
        int offset_y = 0;
        titleLabel.setPosition(0, offset_y);
        titleLabel.setSize(windowWidth, 20);
        offset_y += 30;
        tipLabel.setPosition(10, offset_y);
        Dimension descBounds = getTextDimension(tipLabel.getWidget(), tipLabel.getWidget().getText(), windowWidth - 40);
        tipLabel.setSize(windowWidth - 20, descBounds.height);
        offset_y += descBounds.height + 8;
        int progressBarX = 10;
        int progressBarY = offset_y;
        
        int itemIndex = 0;
        if (currentTask.getVerification() instanceof CollectionLogVerification) {
            CollectionLogVerification verification = (CollectionLogVerification) currentTask.getVerification();
            int[] itemIds = verification.getItemIds();

            int obtainedCount = 0;
            int requiredCount = verification.getCount();
            for (int id : itemIds) {
                if (collectionLogService.isItemObtained(id)) {
                    obtainedCount++;
                }
            }
            int progressBarWidth = windowWidth - 20;
            int progressBarHeight = 18;
            int fillWidth = Math.min((int) ((obtainedCount / (float) requiredCount) * progressBarWidth), progressBarWidth);
            
            progressBarBg.setPosition(progressBarX, progressBarY);
            progressBarBg.setSize(progressBarWidth, progressBarHeight);
            progressBarFill.setPosition(progressBarX, progressBarY);
            progressBarFill.setSize(fillWidth, progressBarHeight);
            progressLabel.setPosition(progressBarX, progressBarY);
            progressLabel.setSize(progressBarWidth, progressBarHeight);
            progressLabel.setText("Obtained " + obtainedCount + "/" + requiredCount + " required items");
            offset_y += 18;

            if (itemIds != null && itemIds.length > 0) {
                int itemSize = 32;
                int spacing = 8;
                int itemsPerRow = Math.max(1, (windowWidth - 20) / (itemSize + spacing));
                int numRows = (int) Math.ceil((double) itemIds.length / itemsPerRow);
                int startY = offset_y + 8;
                int y = startY;
                for (int row = 0; row < numRows; row++) {
                    int itemsInThisRow = Math.min(itemsPerRow, itemIds.length - itemIndex);
                    int totalRowWidth = itemsInThisRow * itemSize + (itemsInThisRow - 1) * spacing;
                    int startX = (windowWidth - totalRowWidth) / 2;
                    int x = startX;
                    // Check if this row would be too close to the bottom edge
                    if (y + itemSize > windowHeight - BUTTON_HEIGHT - 20) {
                        break;
                    }
                    for (int col = 0; col < itemsInThisRow; col++) {
                        UIGraphic itemImage = itemIndex < taskIcons.size() ? taskIcons.get(itemIndex) : null;
                        itemImage.setPosition(x, y);
                        x += itemSize + spacing;
                        itemImage.revalidate();
                        itemIndex++;
                    }
                    y += itemSize + spacing;
                }
                offset_y = y;
                if (itemIndex < itemIds.length) {
                    extraItemsLabel.setText((itemIds.length - itemIndex) + " more items...");
                    extraItemsLabel.setPosition(10, offset_y);
                    extraItemsLabel.setSize(windowWidth, 12);
                    extraItemsLabel.revalidate();
                } else {
                    extraItemsLabel.setText("");
                    extraItemsLabel.setPosition(-100, -100);
                }
                for (int i = itemIndex; i < taskIcons.size(); i++) {
                    UIGraphic itemImage = taskIcons.get(i);
                    if (itemImage != null) {
                        itemImage.setPosition(-100, -100);
                        itemImage.revalidate();
                    }
                }
            }
        } else {
            progressBarBg.setPosition(-100, -100);
            progressBarFill.setPosition(-100, -100);
            progressLabel.setPosition(-100, -100);
        }
        offset_y += 16;
        wikiBtn.setPosition(10, windowHeight - 10 - BUTTON_HEIGHT);
        completeBtn.setPosition((windowWidth / 2) - (LARGE_BUTTON_WIDTH / 2), windowHeight - 10 - BUTTON_HEIGHT);
        closeBtn.setPosition(windowWidth - 10 - BUTTON_WIDTH, windowHeight - 10 - BUTTON_HEIGHT);

        titleLabel.revalidate();
        tipLabel.revalidate();
        wikiBtn.revalidate();
        closeBtn.revalidate();
        completeBtn.revalidate();
        progressBarBg.revalidate();
        progressBarFill.revalidate();
        progressLabel.revalidate();
    }

    private Dimension getTextDimension(Widget widget, String text, int maxWidth) {
        if (text == null || text.isEmpty()) {
            return new Dimension(0, 0);
        }
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        int lineCount = 1;
        int maxLineWidth = 0;
        for (int i = 0; i < words.length; i++) {
            String testLine = line.length() == 0 ? words[i] : line + " " + words[i];
            int testWidth = widget.getFont().getTextWidth(testLine);
            if (line.toString().contains("<br>") || testWidth > maxWidth && line.length() > 0) {
                // Start new line
                maxLineWidth = Math.max(maxLineWidth, widget.getFont().getTextWidth(line.toString()));
                line = new StringBuilder(words[i]);
                lineCount++;
            } else {
                if (line.length() > 0) {
                    line.append(" ");
                }
                line.append(words[i]);
            }
        }
        // Check last line
        if (line.length() > 0) {
            maxLineWidth = Math.max(maxLineWidth, widget.getFont().getTextWidth(line.toString()));
        }
        int lineHeight = widget.getFont().getBaseline();
        return new Dimension(maxLineWidth, lineCount * lineHeight);
    }

    public void closeTask() {
        // TODO: Implement closing the task info
        this.setVisibility(false);
    }

    public void updateBounds()
    {
        Widget wrapper = window.getParent();
        wrapperX = wrapper.getRelativeX();
        wrapperY = wrapper.getRelativeY();
        wrapperHeight = window.getHeight() - OFFSET_Y;
        windowX = window.getRelativeX();
        windowY = window.getRelativeY();
        windowWidth = window.getWidth();
        windowHeight = window.getHeight();

        bounds.setLocation(wrapperX + windowX + OFFSET_X, wrapperY + windowY + OFFSET_Y);
        bounds.setSize(windowWidth - OFFSET_X, wrapperHeight);

        if (!this.isVisible()) {
            return;
        }

        this.setPositions();
    }
}
