package com.collectionlogmaster.ui.component;

import com.collectionlogmaster.CollectionLogMasterPlugin;
import com.collectionlogmaster.taskapp.TaskService;
import com.collectionlogmaster.ui.generic.UIComponent;
import com.collectionlogmaster.ui.generic.UIUtil;
import com.collectionlogmaster.ui.generic.button.UIButton.State;
import com.collectionlogmaster.ui.generic.button.UITextButton;
import javax.inject.Inject;
import net.runelite.api.FontID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetPositionMode;
import net.runelite.api.widgets.WidgetSizeMode;
import net.runelite.api.widgets.WidgetTextAlignment;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.callback.ClientThread;

public class LoginRequiredOverlay extends UIComponent<LoginRequiredOverlay> {
	public static final int BASE_GAP = 16;
	public static final int TITLE_HEIGHT = 50;
	public static final int BUTTON_HEIGHT = 30;
	public static final int BUTTON_WIDTH = 140;
	public static final int BODY_HEIGHT = 70;

	private final Widget title;
	private final Widget body;
	private final UITextButton taskAppButton;

	public static LoginRequiredOverlay createInside(Widget window) {
		return new LoginRequiredOverlay(window.createChild(WidgetType.LAYER));
	}

	protected LoginRequiredOverlay(Widget widget) {
		super(widget, WidgetType.LAYER);
		CollectionLogMasterPlugin.getStaticInjector().injectMembers(this);

		title = widget.createChild(WidgetType.TEXT);
		body = widget.createChild(WidgetType.TEXT);
		taskAppButton = UITextButton.createInside(widget);

		initializeWidgets();
	}

	private void initializeWidgets() {
		widget.setPos(0, 0)
			.setWidthMode(WidgetSizeMode.MINUS)
			.setHeightMode(WidgetSizeMode.MINUS)
			.setSize(0, 0)
			.revalidate();

		title.setXPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
			.setPos(0, BASE_GAP)
			.setWidthMode(WidgetSizeMode.MINUS)
			.setSize(0, TITLE_HEIGHT)
			.setFontId(FontID.QUILL_CAPS_LARGE)
			.setTextColor(0xFFA0A0)
			.setTextShadowed(true)
			.setXTextAlignment(WidgetTextAlignment.CENTER)
			.setYTextAlignment(WidgetTextAlignment.CENTER)
			.setText("Login Required")
			.revalidate();

		body.setXPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
			.setPos(0, title.getRelativeY() + title.getHeight() + BASE_GAP)
			.setWidthMode(WidgetSizeMode.MINUS)
			.setSize(BASE_GAP * 2, BODY_HEIGHT)
			.setFontId(FontID.BOLD_12)
			.setTextColor(0xFFFFFF)
			.setTextShadowed(true)
			.setXTextAlignment(WidgetTextAlignment.CENTER)
			.setYTextAlignment(WidgetTextAlignment.CENTER)
			.setText(
				"You need to input valid TaskApp credentials in the plugin configuration in order to use the it!"
				+ " If you don't have an account, click the button below to visit the website and register."
				+ "<br><br>Close the dashboard and open it again after doing that."
			)
			.revalidate();

		taskAppButton.setXPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
			.setPos(0, body.getRelativeY() + body.getHeight() + BASE_GAP)
			.setSize(BUTTON_WIDTH, BUTTON_HEIGHT)
			.setText("Visit TaskApp")
			.setName(UIUtil.formatName("TaskApp"))
			.setAction("Visit", UIUtil::openTaskApp)
			.revalidate();
	}

	@Override
	public void revalidate() {
		super.revalidate();

		title.revalidate();
		body.revalidate();
		taskAppButton.revalidate();
	}
}
