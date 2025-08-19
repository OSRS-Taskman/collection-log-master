package com.collectionlogmaster.ui.component;

import com.collectionlogmaster.CollectionLogMasterConfig;
import com.collectionlogmaster.CollectionLogMasterPlugin;
import com.collectionlogmaster.domain.Task;
import com.collectionlogmaster.domain.TaskTier;
import com.collectionlogmaster.synchronization.SyncService;
import com.collectionlogmaster.task.TaskService;
import com.collectionlogmaster.ui.generic.UIGraphic;
import com.collectionlogmaster.ui.generic.UILabel;
import com.collectionlogmaster.ui.generic.UINativeButton;
import com.collectionlogmaster.ui.generic.UIPage;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.FontID;
import net.runelite.api.SoundEffectID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

import static com.collectionlogmaster.ui.InterfaceConstants.COLLECTION_LOG_WINDOW_HEIGHT;
import static com.collectionlogmaster.ui.InterfaceConstants.COLLECTION_LOG_WINDOW_WIDTH;

public class TaskDashboard extends UIPage {
    private final static int DEFAULT_BUTTON_WIDTH = 140;
    private final static int DEFAULT_BUTTON_HEIGHT = 30;
    private final static int SMALL_BUTTON_WIDTH = 68;
    private final static int DEFAULT_TASK_DETAILS_WIDTH = 300;
    private final static int DEFAULT_TASK_DETAILS_HEIGHT = 75;
    private final static int TASK_BACKGROUND_SPRITE_ID = -20006;

    @Getter
    private Widget window;
    private CollectionLogMasterPlugin plugin;

    private CollectionLogMasterConfig config;
    private final SyncService syncService;
    private final TaskService taskService;
    private final Client client;
    private final TaskInfo taskInfo;

    private UILabel title;
    private UILabel taskLabel;
    private UILabel percentCompletion;

    private UIGraphic taskImage;
    private UIGraphic taskBg;

    private final UINativeButton completeTaskBtn;
    private final UINativeButton generateTaskBtn;
    private final UINativeButton faqBtn;
    private final UINativeButton syncBtn;

    public TaskDashboard(CollectionLogMasterPlugin plugin, CollectionLogMasterConfig config, Widget window, SyncService syncService, TaskService taskService, Client client, TaskInfo taskInfo) {
        this.window = window;
        this.plugin = plugin;
        this.config = config;
        this.syncService = syncService;
        this.taskService = taskService;
        this.client = client;
        this.taskInfo = taskInfo;

        createTaskDetails();

        Widget titleWidget = window.createChild(-1, WidgetType.TEXT);
        this.title = new UILabel(titleWidget);
        this.title.setFont(FontID.QUILL_CAPS_LARGE);
        this.title.setSize(COLLECTION_LOG_WINDOW_WIDTH, DEFAULT_TASK_DETAILS_HEIGHT);
        this.title.setPosition(getCenterX(window, COLLECTION_LOG_WINDOW_WIDTH), 24);
        this.title.setText("Current Task");

        Widget percentWidget = window.createChild(-1, WidgetType.TEXT);
        this.percentCompletion = new UILabel(percentWidget);
        this.percentCompletion.setFont(FontID.BOLD_12);
        this.percentCompletion.setSize(COLLECTION_LOG_WINDOW_WIDTH, 25);
        this.percentCompletion.setPosition(getCenterX(window, COLLECTION_LOG_WINDOW_WIDTH), COLLECTION_LOG_WINDOW_HEIGHT - 91);
        updatePercentages();

        completeTaskBtn = new UINativeButton(window.createChild(WidgetType.LAYER));
        completeTaskBtn.setSize(DEFAULT_BUTTON_WIDTH, DEFAULT_BUTTON_HEIGHT);
        completeTaskBtn.setPosition(getCenterX(window, DEFAULT_BUTTON_WIDTH) + (DEFAULT_BUTTON_WIDTH / 2 + 15), getCenterY(window, DEFAULT_BUTTON_HEIGHT) + 62);
        completeTaskBtn.setText("Complete Task");
        completeTaskBtn.addAction("Complete", plugin::completeTask);

        generateTaskBtn = new UINativeButton(window.createChild(WidgetType.LAYER));
        generateTaskBtn.setSize(DEFAULT_BUTTON_WIDTH, DEFAULT_BUTTON_HEIGHT);
        generateTaskBtn.setPosition(getCenterX(window, DEFAULT_BUTTON_WIDTH) - (DEFAULT_BUTTON_WIDTH / 2 + 15), getCenterY(window, DEFAULT_BUTTON_HEIGHT) + 62);
        generateTaskBtn.setText("Generate Task");
        generateTaskBtn.addAction("Generate task", this::generateTask);

        faqBtn = new UINativeButton(window.createChild(WidgetType.LAYER));
        faqBtn.setSize(SMALL_BUTTON_WIDTH, DEFAULT_BUTTON_HEIGHT);
        faqBtn.setPosition(getCenterX(window, SMALL_BUTTON_WIDTH) + 190, getCenterY(window, DEFAULT_BUTTON_HEIGHT) + 112);
        faqBtn.setText("FAQ");
        faqBtn.addAction("Open FAQ", plugin::visitFaq);

        syncBtn = new UINativeButton(window.createChild(WidgetType.LAYER));
        syncBtn.setSize(SMALL_BUTTON_WIDTH, DEFAULT_BUTTON_HEIGHT);
        syncBtn.setPosition(getCenterX(window, SMALL_BUTTON_WIDTH) - 190, getCenterY(window, DEFAULT_BUTTON_HEIGHT) + 112);
        syncBtn.setText("Sync");
        syncBtn.addAction("Sync completed tasks", syncService::sync);

        this.add(this.title);
        this.add(this.taskBg);
        this.add(this.taskLabel);
        this.add(this.taskImage);
        this.add(this.percentCompletion);
        this.add(completeTaskBtn);
        this.add(generateTaskBtn);
        this.add(faqBtn);
        this.add(syncBtn);
    }

    @Override
    public void setVisibility(boolean visible) {
        super.setVisibility(visible);
        if (visible) {
            if (taskService.getActiveTask() == null) {
                clearTask();
            } else {
                Task activeTask = taskService.getActiveTask();
                setTask(activeTask, null);
            }
        }
    }

    private void createTaskDetails() {
        final int POS_X = getCenterX(window, DEFAULT_TASK_DETAILS_WIDTH);
        final int POS_Y = getCenterY(window, DEFAULT_TASK_DETAILS_HEIGHT)-3;

        Widget taskBgWidget = window.createChild(-1, WidgetType.GRAPHIC);
        this.taskBg = new UIGraphic(taskBgWidget);
        this.taskBg.setSize(DEFAULT_TASK_DETAILS_WIDTH, DEFAULT_TASK_DETAILS_HEIGHT);
        this.taskBg.setPosition(POS_X, POS_Y);
        this.taskBg.setSprite(TASK_BACKGROUND_SPRITE_ID);

        Widget label = window.createChild(-1, WidgetType.TEXT);
        label.setTextColor(Color.WHITE.getRGB());
        label.setTextShadowed(true);
        label.setName("Task Label");
        this.taskLabel = new UILabel(label);
        this.taskLabel.setFont(496);
        this.taskLabel.setPosition(POS_X+60, POS_Y);
        this.taskLabel.setSize(DEFAULT_TASK_DETAILS_WIDTH-60, DEFAULT_TASK_DETAILS_HEIGHT);

        Widget taskImageWidget = window.createChild(-1, WidgetType.GRAPHIC);
        this.taskImage = new UIGraphic(taskImageWidget);
        this.taskImage.setPosition(POS_X+12, POS_Y+21);
        this.taskImage.setSize(42, 36);
        this.taskImage.getWidget().setBorderType(1);
    }

    public void clearTask() {
        this.taskBg.getWidget().clearActions();
        this.taskBg.clearActions();
        this.taskLabel.setText("No active task.");
        this.taskImage.setItem(7542);
        this.disableCompleteTask();
        this.enableGenerateTask();
    }

    public void setTask(Task task, List<Task> cyclingTasks) {
        this.disableGenerateTask();

        if (cyclingTasks != null) {
            for (int i = 0; i < 250; i++) {
                Task displayTask = cyclingTasks.get((int) Math.floor(Math.random() * cyclingTasks.size()));
                // Seems the most natural timing
                double decay = 450.0 / ((double) config.rollTime());
                int delay = (int) ((config.rollTime() * 0.925) * Math.exp(-decay * i));
                Timer fakeTaskTimer = new Timer(delay, ae -> {
                    this.taskLabel.setText(displayTask.getName());
                    this.taskImage.setItem(displayTask.getDisplayItemId());
                });
                fakeTaskTimer.setRepeats(false);
                fakeTaskTimer.setCoalesce(true);
                fakeTaskTimer.start();
            }
            Timer realTaskTimer = new Timer(config.rollTime(), ae -> {
                setTask(task, null);
            });
            realTaskTimer.setRepeats(false);
            realTaskTimer.setCoalesce(true);
            realTaskTimer.start();
            return;
        }

        this.taskLabel.setText(task.getName());
        this.taskImage.setItem(task.getDisplayItemId());
        this.taskBg.clearActions();
        this.taskBg.addAction("View task info", () -> taskInfo.showTask(task.getId()));
        this.enableCompleteTask();
    }

    private void generateTask() {
		client.playSoundEffect(SoundEffectID.UI_BOOP);
		Task generatedTask = taskService.generate();

		List<Task> rollTaskList = config.rollPastCompleted() ? taskService.getTierTasks() : taskService.getIncompleteTierTasks();
		setTask(generatedTask, rollTaskList);
        disableGenerateTask();
        updatePercentages();
	}

    public void updatePercentages() {
        Map<TaskTier, Float> progress = taskService.getProgress();
        TaskTier currentTier = taskService.getCurrentTier();
        float tierPercentage = progress.get(currentTier);

        String text = String.format(
                "<col=%s>%d%%</col> %s Completed",
                getCompletionColor(tierPercentage),
                (int) tierPercentage,
                currentTier.displayName
        );
        percentCompletion.setText(text);
    }

    private String getCompletionColor(double percent) {
        int max = 255;
        int amount = (int) Math.round(((percent % 50) / 50) * max);

        if(percent == 100) {
            return "00ff00";
        }
        else if(percent > 50) {
            int redValue = max - amount;
            return String.format("%02x", redValue)+"ff00";

        }
        else if(percent == 50) {
            return "ffff00";
        }
        else {
            return "ff"+String.format("%02x", amount)+"00";
        }
    }


    public void disableGenerateTask() {
        generateTaskBtn.setState(UINativeButton.State.DISABLED);
    }

    public void enableGenerateTask() {
        generateTaskBtn.setState(UINativeButton.State.DEFAULT);

        this.disableCompleteTask();
    }

    public void disableCompleteTask() {
        completeTaskBtn.setState(UINativeButton.State.DISABLED);
    }

    public void enableCompleteTask() {
        completeTaskBtn.setState(UINativeButton.State.DEFAULT);
    }

    public void updateBounds() {
        int windowWidth = window.getWidth();

        // Update title position - force widget position update
        int titleX = getCenterX(window, COLLECTION_LOG_WINDOW_WIDTH);
        this.title.setPosition(titleX, 24);
        this.title.getWidget().setPos(titleX, 24);

        // Update task details (background, label, image)
        final int taskPosX = getCenterX(window, DEFAULT_TASK_DETAILS_WIDTH);
        final int taskPosY = getCenterY(window, DEFAULT_TASK_DETAILS_HEIGHT) - 3;
        
        this.taskBg.setPosition(taskPosX, taskPosY);
        this.taskBg.getWidget().setPos(taskPosX, taskPosY);
        
        this.taskLabel.setPosition(taskPosX + 60, taskPosY);
        this.taskLabel.getWidget().setPos(taskPosX + 60, taskPosY);
        
        this.taskImage.setPosition(taskPosX + 12, taskPosY + 21);
        this.taskImage.getWidget().setPos(taskPosX + 12, taskPosY + 21);

        // Update button positions - force widget position updates
        int generateBtnX = getCenterX(window, DEFAULT_BUTTON_WIDTH) - (DEFAULT_BUTTON_WIDTH / 2 + 15);
        int generateBtnY = getCenterY(window, DEFAULT_BUTTON_HEIGHT) + 62;
        generateTaskBtn.setPosition(generateBtnX, generateBtnY);
        
        int completeBtnX = getCenterX(window, DEFAULT_BUTTON_WIDTH) + (DEFAULT_BUTTON_WIDTH / 2 + 15);
        int completeBtnY = getCenterY(window, DEFAULT_BUTTON_HEIGHT) + 62;
        completeTaskBtn.setPosition(completeBtnX, completeBtnY);
        completeTaskBtn.getWidget().setPos(completeBtnX, completeBtnY);
        
        // Update FAQ button position with boundary checking
        int faqBtnX = getCenterX(window, SMALL_BUTTON_WIDTH) + 238;
        int faqBtnY = getCenterY(window, DEFAULT_BUTTON_HEIGHT) + 112;
        
        // Check if FAQ button would go outside the window and align with edge if needed
        int faqBtnWidth = SMALL_BUTTON_WIDTH;
        if (faqBtnX + faqBtnWidth + 10 > windowWidth) {
            faqBtnX = windowWidth - faqBtnWidth - 10; // 10px margin from edge
        }
        faqBtn.setPosition(faqBtnX, faqBtnY);

        // Update Sync button position with boundary checking
        int syncBtnX = getCenterX(window, SMALL_BUTTON_WIDTH) - 238;
        int syncBtnY = getCenterY(window, DEFAULT_BUTTON_HEIGHT) + 112;
        if (syncBtnX < 10) {
            syncBtnX = 10; // 10px margin from left edge
        }
        syncBtn.setPosition(syncBtnX, syncBtnY);

        // Update percentage completion position - force widget position update
        int percentX = getCenterX(window, COLLECTION_LOG_WINDOW_WIDTH);
        int percentY = getCenterY(window, DEFAULT_BUTTON_HEIGHT) + 112; // Same Y as FAQ button
        this.percentCompletion.setPosition(percentX, percentY);
        this.percentCompletion.getWidget().setPos(percentX, percentY);
        
        // Force revalidation of all widgets
        this.title.getWidget().revalidate();
        this.taskBg.getWidget().revalidate();
        this.taskLabel.getWidget().revalidate();
        this.taskImage.getWidget().revalidate();
        generateTaskBtn.revalidate();
        completeTaskBtn.revalidate();
        faqBtn.revalidate();
        syncBtn.revalidate();
        this.percentCompletion.getWidget().revalidate();
    }

	private int getCenterX(Widget window, int width) {
		return (window.getWidth() / 2) - (width / 2);
	}

	private int getCenterY(Widget window, int height) {
		return (window.getHeight() / 2) - (height / 2);
	}

	private void playFailSound() {
		client.playSoundEffect(2277);
	}
}
