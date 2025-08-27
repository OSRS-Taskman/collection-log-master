package com.collectionlogmaster.ui.neww.button;

import com.collectionlogmaster.ui.generic.MenuAction;
import com.collectionlogmaster.ui.neww.UIComponent;
import lombok.Getter;
import net.runelite.api.ScriptEvent;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;

public abstract class UIButton<This extends UIButton<This>> extends UIComponent<This> {
	public enum State {
		DEFAULT,
		HOVER,
		DISABLED;
	}

	@Getter
	private State state = State.DEFAULT;

	protected MenuAction action = null;

	protected UIButton(Widget widget) {
		super(widget, WidgetType.LAYER);

		widget.setOnOpListener((JavaScriptCallback) this::onActionSelected);
		widget.setOnMouseOverListener((JavaScriptCallback) this::onMouseHover);
		widget.setOnMouseLeaveListener((JavaScriptCallback) this::onMouseLeave);
		widget.setHasListener(true);

		createChildren(widget);
	}

	protected abstract void createChildren(Widget widget);

	protected void onActionSelected(ScriptEvent e) {
		if (state == State.DISABLED) return;

		if (action != null) {
			action.onMenuAction();
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

	@SuppressWarnings("unchecked")
	public This setState(State state) {
		this.state = state;

		return (This) this;
	}

	@SuppressWarnings("unchecked")
	public This setAction(String label, MenuAction action) {
		widget.setAction(0, label);
		this.action = action;

		return (This) this;
	}
}
