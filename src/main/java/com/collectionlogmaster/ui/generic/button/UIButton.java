package com.collectionlogmaster.ui.generic.button;

import com.collectionlogmaster.ui.generic.UIComponent;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.runelite.api.ScriptEvent;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;

@Accessors(chain = true)
public abstract class UIButton<This extends UIButton<This>> extends UIComponent<This> {
	public enum State {
		DEFAULT,
		HOVER,
		DISABLED;
	}

	@Getter
	@Setter
	private State state = State.DEFAULT;

	@Getter
	protected Runnable action = null;

	protected UIButton(Widget widget) {
		super(widget, WidgetType.LAYER);

		widget.setOnOpListener((JavaScriptCallback) this::onActionSelected);
		widget.setOnMouseOverListener((JavaScriptCallback) this::onMouseHover);
		widget.setOnMouseLeaveListener((JavaScriptCallback) this::onMouseLeave);
		widget.setHasListener(true);
	}

	protected void onActionSelected(ScriptEvent e) {
		if (state == State.DISABLED) {
			return;
		}

		if (action != null) {
			action.run();
		}
	}

	protected void onMouseHover(ScriptEvent e) {
		if (state == State.DEFAULT) {
			setState(State.HOVER)
				.revalidate();
		}
	}

	protected void onMouseLeave(ScriptEvent e) {
		if (state == State.HOVER) {
			setState(State.DEFAULT)
				.revalidate();
		}
	}

	public This setName(String name) {
		widget.setName(name);
		return castThis();
	}

	public This setAction(String label, Runnable action) {
		widget.setAction(0, label);
		this.action = action;

		return castThis();
	}

	public void triggerAction() {
		onActionSelected(null);
	}
}
