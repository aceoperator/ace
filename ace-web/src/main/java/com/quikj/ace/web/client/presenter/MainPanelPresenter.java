/**
 * 
 */
package com.quikj.ace.web.client.presenter;

import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.quikj.ace.web.client.ApplicationController;
import com.quikj.ace.web.client.RootPanelResizeListener;
import com.quikj.ace.web.client.comm.CommunicationsFactory;
import com.quikj.ace.web.client.view.MainPanel;

/**
 * @author amit
 * 
 */
public class MainPanelPresenter {

	private MainPanel view;

	private Logger logger;

	public static final String BROWSER_TABLET = "tablet";

	public static final String BROWSER_MOBILE = "mobile";

	public static final String BROWSER_DESKTOP = "desktop";

	private static MainPanelPresenter instance = null;

	private MainPanelPresenter() {
		logger = Logger.getLogger(getClass().getName());
		view = createView();
		view.setPresenter(this);
	}

	public static MainPanelPresenter getInstance() {
		if (instance == null) {
			instance = new MainPanelPresenter();
		}

		return instance;
	}

	private MainPanel createView() {
		return (MainPanel)GWT.create(MainPanel.class);
	}

	public void show() {
		ApplicationController.getInstance().addResizeListener(
				(RootPanelResizeListener) view);
		view.attach(ApplicationController.getInstance().getRootPanel());
	}

	public void dispose() {
		view.detach(ApplicationController.getInstance().getRootPanel());

		ApplicationController.getInstance().removeResizeListener(
				(RootPanelResizeListener) view);
		view = null;
	}

	public void attachToMainPanel(Widget widget) {
		view.setWidget(widget);
	}

	public void detachFromMainPanel() {
		view.removeWidget();
	}

	public LayoutPanel getMainPanel() {
		return (LayoutPanel) view;
	}

	public void applicationClose() {
		if (!CommunicationsFactory.getServerCommunications().isConnected()) {
			return;
		}

		MessageBoxPresenter.getInstance().show(
				ApplicationController.getMessages().MainPanelPresenter_error(),
				ApplicationController.getMessages()
						.MainPanelPresenter_navigatingAway(),
				MessageBoxPresenter.Severity.WARN, true);

		logger.info("Application closed ungracefully");
		CommunicationsFactory.getServerCommunications().disconnect();
	}
	
	public void popup(Widget w) {
		view.popup(w);
	}
	
	public void popdown() {
		view.popdown();
	}
	
	public String getBrowserType() {
		return view.browserType();
	}
}
