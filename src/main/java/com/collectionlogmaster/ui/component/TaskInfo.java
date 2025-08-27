package com.collectionlogmaster.ui.component;

import com.collectionlogmaster.CollectionLogMasterPlugin;
import com.collectionlogmaster.domain.Task;
import com.collectionlogmaster.domain.verification.Verification;
import com.collectionlogmaster.domain.verification.clog.CollectionLogVerification;
import com.collectionlogmaster.domain.verification.diary.AchievementDiaryVerification;
import com.collectionlogmaster.domain.verification.diary.DiaryDifficulty;
import com.collectionlogmaster.domain.verification.diary.DiaryRegion;
import com.collectionlogmaster.domain.verification.skill.SkillVerification;
import com.collectionlogmaster.synchronization.clog.CollectionLogService;
import com.collectionlogmaster.synchronization.diary.AchievementDiaryService;
import com.collectionlogmaster.task.TaskService;
import com.collectionlogmaster.ui.generic.UIComponent;
import com.collectionlogmaster.ui.neww.UIGridContainer;
import com.collectionlogmaster.ui.neww.UIProgressBar;
import com.collectionlogmaster.ui.neww.UIScrollableContainer;
import com.collectionlogmaster.ui.neww.UIUtil;
import com.collectionlogmaster.ui.neww.button.UISimpleButton;
import com.collectionlogmaster.ui.neww.button.UITextButton;
import com.google.inject.Inject;
import java.awt.Color;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.FontID;
import net.runelite.api.gameval.SpriteID;
import net.runelite.api.widgets.ItemQuantityMode;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetPositionMode;
import net.runelite.api.widgets.WidgetSizeMode;
import net.runelite.api.widgets.WidgetTextAlignment;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.game.ItemManager;
import net.runelite.client.util.LinkBrowser;
import org.apache.commons.lang3.tuple.Pair;


@Slf4j
public class TaskInfo extends UIComponent {
	private static final int TITLE_HEIGHT = 24;
	private static final int BASE_GAP = 4;
	public static final int PROGRESS_BAR_HEIGHT = 24;
	public static final int BUTTON_HEIGHT = 30;
	public static final int BUTTON_WIDTH = 140;

	private final Widget background;
	private final Widget titleText;
	private final UISimpleButton backButton;
	private final UISimpleButton wikiButton;
	private final Widget divider;
	private final Widget tipText;
	private final UIProgressBar progressBar;
	private final UITextButton markButton;
	private final UIScrollableContainer scrollableContainer;
	private final UIGridContainer itemGrid;

	@Inject
	private Client client;

	@Inject
	private ItemManager itemManager;

	@Inject
	private TaskService taskService;

	@Inject
	private CollectionLogService collectionLogService;

	@Inject
	private AchievementDiaryService achievementDiaryService;

	private final @NonNull Task task;

	private final CompletableFuture<Void> closeFuture;

	public static CompletableFuture<Void> openInside(Widget window, @NonNull Task task) {
		Widget widget = window.createChild(WidgetType.LAYER);
		return new TaskInfo(widget, task).closeFuture;
	}

	private TaskInfo(Widget widget, @NonNull Task task) {
		super(widget, Set.of(WidgetType.LAYER));
		CollectionLogMasterPlugin.getStaticInjector().injectMembers(this);

		closeFuture = new CompletableFuture<>();
		this.task = task;

		background = widget.createChild(WidgetType.GRAPHIC);
		titleText = widget.createChild(WidgetType.TEXT);
		backButton = UISimpleButton.createInside(widget);
		wikiButton = UISimpleButton.createInside(widget);
		divider = widget.createChild(WidgetType.RECTANGLE);
		tipText = widget.createChild(WidgetType.TEXT);
		progressBar = UIProgressBar.createInside(widget);
		scrollableContainer = UIScrollableContainer.createInside(widget, WidgetType.LAYER);
		itemGrid = new UIGridContainer(scrollableContainer.getContent());
		markButton = UITextButton.createInside(widget);

		applyStaticStyles();
		applyStatefulStyles();
	}

	private void applyStatefulStyles() {

		itemGrid.clearItems();
		if (task.getVerification() instanceof CollectionLogVerification) {
			CollectionLogVerification verif = (CollectionLogVerification) task.getVerification();
			for (int itemId : verif.getItemIds()) {
				String itemName = itemManager.getItemComposition(itemId).getMembersName();
				boolean itemObtained = collectionLogService.isItemObtained(itemId);

				Widget w = itemGrid.createItem(WidgetType.GRAPHIC)
						.setName(UIUtil.formatName(itemName))
						.setSize(36, 32)
						.setOpacity(itemObtained ? 0 : 175)
						.setItemQuantityMode(ItemQuantityMode.NEVER)
						.setItemQuantity(1000)
						.setItemId(itemId);

				w.setOnOpListener((JavaScriptCallback) e -> UIUtil.openWikiLink(itemName));
				w.setHasListener(true);
				w.setAction(0, "Wiki");
				w.setBorderType(1);
				w.revalidate();
			}
		}
		itemGrid.revalidate();
	}

	private void applyStaticStyles() {
		widget.setWidthMode(WidgetSizeMode.MINUS)
				.setHeightMode(WidgetSizeMode.MINUS)
				.setSize(0, 0)
				.revalidate();

		background.setPos(0, 0)
				.setWidthMode(WidgetSizeMode.MINUS)
				.setHeightMode(WidgetSizeMode.MINUS)
				.setSize(0, 0)
				.setSpriteId(SpriteID.TRADEBACKING)
				.setSpriteTiling(true)
				.revalidate();

		titleText.setXPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
				.setPos(0, 0)
				.setWidthMode(WidgetSizeMode.MINUS)
				.setSize(0, TITLE_HEIGHT)
				.setLineHeight(TITLE_HEIGHT)
				.setXTextAlignment(WidgetTextAlignment.CENTER)
				.setYTextAlignment(WidgetTextAlignment.CENTER)
				.setFontId(FontID.BOLD_12)
				.setTextShadowed(true)
				.setTextColor(Color.WHITE.getRGB())
				.setText(task.getName())
				.revalidate();

		backButton.setPos(BASE_GAP / 2, BASE_GAP / 2)
				.setSize(40, titleText.getHeight() - BASE_GAP)
				.setIconSpriteTheme(SpriteID.CloseArrows._0, SpriteID.CloseArrows._1, SpriteID.CloseArrows._0)
				.setIconSize(13, 11)
				.setFontId(FontID.PLAIN_11)
				.setText("Back")
				.setAction("Go back", this::close)
				.revalidate();

		wikiButton
				.setXPositionMode(WidgetPositionMode.ABSOLUTE_RIGHT)
				.setPos(BASE_GAP / 2, BASE_GAP / 2)
				.setSize(40, titleText.getHeight() - BASE_GAP)
				.setIconSpriteTheme(SpriteID.WikiIcon.DESELECTED, SpriteID.WikiIcon.SELECTED, SpriteID.WikiIcon.DESELECTED)
				.setIconSize(40, 14)
				.setFontId(FontID.PLAIN_11)
				.setText("")
				.setAction("Wiki", () -> LinkBrowser.browse(task.getWikiLink()))
				.revalidate();

		divider.setXPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
				.setPos(0, titleText.getRelativeY() + titleText.getHeight())
				.setWidthMode(WidgetSizeMode.MINUS)
				.setSize(BASE_GAP * 2, 1)
				.setTextColor(0x606060)
				.revalidate();

		String tip = task.getTip();
		if (tip == null || tip.isBlank()) {
			tipText.setHidden(true)
					.revalidate();
		} else {
			tipText.setXPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
					.setPos(0, divider.getRelativeY() + divider.getHeight() + BASE_GAP)
					.setWidthMode(WidgetSizeMode.MINUS)
					.setOriginalWidth(BASE_GAP * 2)
					.setXTextAlignment(WidgetTextAlignment.CENTER)
					.setYTextAlignment(WidgetTextAlignment.CENTER)
					.setFontId(FontID.PLAIN_11)
					.setTextColor(Color.WHITE.getRGB())
					.setText(tip)
					.revalidate();

			// we need to revalidate before setting the height because
			// we require the widget's width to be up to date
			tipText.setOriginalHeight(UIComponent.getTextHeight(tip, tipText))
					.revalidate();
		}

		Widget prev = tipText.isHidden() ? divider : tipText;
		progressBar.setXPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
				.setPos(0, prev.getOriginalY() + prev.getHeight() + BASE_GAP)
				.setWidthMode(WidgetSizeMode.MINUS)
				.setSize(BASE_GAP, PROGRESS_BAR_HEIGHT)
				.revalidate();

		int gridOriginalY = progressBar.getOriginalY() + progressBar.getHeight() + BASE_GAP;
		scrollableContainer.setXPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
				.setPos(0, gridOriginalY)
				.setWidthMode(WidgetSizeMode.MINUS)
				.setHeightMode(WidgetSizeMode.MINUS)
				.setSize(BASE_GAP, gridOriginalY)
				.setScrollBuffer(BUTTON_HEIGHT + (BASE_GAP * 2))
				.revalidate();

		Pair<Float, String> progressData = getProgressData(task.getVerification());
		if (progressData == null) {
			progressBar.setHidden(true)
					.revalidate();
		} else {
			progressBar.setPercent(progressData.getLeft())
					.setText(progressData.getRight())
					.revalidate();
		}

		// TODO: grid

		markButton.setXPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
				.setYPositionMode(WidgetPositionMode.ABSOLUTE_BOTTOM)
				.setPos(0, BASE_GAP)
				// TODO: constants
				.setSize(BUTTON_WIDTH, BUTTON_HEIGHT)
				.setFont(FontID.BOLD_12)
				.setText("Mark Complete")
				.setAction("Mark", () -> {
					taskService.toggleComplete(task.getId());
					applyMarkButtonText();
				})
				.revalidate();

		applyMarkButtonText();
	}

	private void applyMarkButtonText() {
		if (taskService.isComplete(task.getId())) {
			markButton.setText("Mark Incomplete");
		} else {
			markButton.setText("Mark Complete");
		}
	}

	private void close() {
		// we're only "leaking" the parent LAYER widget
		widget.setHidden(true)
				.deleteAllChildren();

		closeFuture.complete(null);
	}

	private Pair<@NonNull Float, @NonNull String> getProgressData(Verification verif) {
		if (verif == null) return null;

		if (verif.isCollectionLog()) {
			return getProgressData(verif.asCollectionLog());
		}

		if (verif.isAchievementDiary()) {
			return getProgressData(verif.asAchievementDiary());
		}

		if (verif.isSkill()) {
			return getProgressData(verif.asSkill());
		}

		return null;
	}

	private Pair<@NonNull Float, @NonNull String> getProgressData(CollectionLogVerification verif) {
		int totalCount = verif.getCount();
		long obtainedCount = Arrays.stream(verif.getItemIds())
				.filter(itemId -> collectionLogService.isItemObtained(itemId))
				.count();

		return Pair.of(
				Math.min(1, (float) obtainedCount / totalCount),
				String.format("Obtained %d/%d required items", obtainedCount, totalCount)
		);
	}

	private Pair<@NonNull Float, @NonNull String> getProgressData(AchievementDiaryVerification verif) {
		DiaryRegion region = verif.getRegion();
		DiaryDifficulty difficulty = verif.getDifficulty();
		int totalCount = achievementDiaryService.getTotalTaskCount(region, difficulty);
		int completedCount = achievementDiaryService.getCompleteTaskCount(region, difficulty);

		return Pair.of(
				(float) completedCount / totalCount,
				String.format("Completed %d/%d required tasks", completedCount, totalCount)
		);
	}

	private Pair<@NonNull Float, @NonNull String> getProgressData(SkillVerification verif) {
		int totalCount = verif.getCount();
		long achievedCount = verif.getExperience().entrySet().stream()
				.filter(entry -> entry.getKey() != null)
				.filter(entry -> client.getSkillExperience(entry.getKey()) > entry.getValue())
				.count();

		return Pair.of(
				(float) achievedCount / totalCount,
				String.format("Achieved in %d/%d required skills", achievedCount, totalCount)
		);
	}

	@Override
	public void revalidate() {
		super.revalidate();
		titleText.revalidate();
		backButton.revalidate();
		wikiButton.revalidate();
		tipText.revalidate();
		progressBar.revalidate();
		scrollableContainer.revalidate();
		itemGrid.revalidate();
		markButton.revalidate();
	}
}
