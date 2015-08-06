package com.quikj.ace.web.client.view;

import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.quikj.ace.web.client.presenter.MainPanelPresenter;

public interface MainPanel {

	public void attach(RootPanel rootPanel);
	public void detach(RootPanel rootPanel);
	public Panel getMainPanel();
	public void setPresenter(MainPanelPresenter presenter);
	public void setWidget(Widget widget);
	public void removeWidget();
	public void popup(Widget w);
	public void popdown();
	public String browserType();
}
