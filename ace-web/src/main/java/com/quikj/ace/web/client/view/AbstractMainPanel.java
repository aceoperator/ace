/**
 * 
 */
package com.quikj.ace.web.client.view;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ClosingEvent;
import com.google.gwt.user.client.Window.ClosingHandler;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.quikj.ace.web.client.RootPanelResizeListener;
import com.quikj.ace.web.client.presenter.MainPanelPresenter;
import com.quikj.ace.web.client.view.MainPanel;

/**
 * @author amit
 * 
 */
public abstract class AbstractMainPanel extends LayoutPanel implements MainPanel,
		RootPanelResizeListener {

	private MainPanelPresenter presenter;
	private Widget savedWidget;
	private boolean popped = false;

	public AbstractMainPanel() {
		super();
		setSize("100%", "100%");

		Window.enableScrolling(false);

		Window.addWindowClosingHandler(new ClosingHandler() {

			@Override
			public void onWindowClosing(ClosingEvent event) {
				presenter.applicationClose();
			}
		});
	}

	@Override
	public Panel getMainPanel() {
		return this;
	}

	@Override
	public void attach(RootPanel root) {
		root.add(this);
	}

	@Override
	public void detach(RootPanel panel) {
		panel.remove(this);
	}

	@Override
	public void setPresenter(MainPanelPresenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void setWidget(Widget widget) {
		removeWidget();
		addWidget(widget);
	}

	private void addWidget(Widget widget) {
		add(widget);
		setWidgetTopHeight(widget, 0, Style.Unit.PCT, 100, Style.Unit.PCT);
		setWidgetLeftWidth(widget, 0, Style.Unit.PCT, 100, Style.Unit.PCT);
	}

	@Override
	public void removeWidget() {
		if (getWidgetCount() > 0) {
			remove(0);
		}
	}

	@Override
	public void onResize(int width, int height, Layout layout) {
		onResize();
	}

	@Override
	public void popup(Widget w) {
		if (popped) {
			return;
		}
		
		if (getWidgetCount() > 0) {
			savedWidget = getWidget(0);
			savedWidget.setVisible(false);
		}
		
		add(w);
		popped = true;
	}

	@Override
	public void popdown() {
		if (!popped) {
			return;
		}
				
		if (getWidgetCount() > 0) {
			remove(0);
		}
		
		if (savedWidget != null) {
			savedWidget.setVisible(true);
			savedWidget = null;
		}
		
		popped = false;
	}
}
