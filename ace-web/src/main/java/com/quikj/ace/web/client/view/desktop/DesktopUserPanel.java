/**
 * 
 */
package com.quikj.ace.web.client.view.desktop;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.quikj.ace.web.client.presenter.UserPanelPresenter;
import com.quikj.ace.web.client.view.UserPanel;

/**
 * @author beckie
 * 
 */
public class DesktopUserPanel extends TabLayoutPanel implements UserPanel {

	public static final int TABBAR_HEIGHT = 31;
	private UserPanelPresenter presenter;
	private boolean handlersInitialized = false;
	private boolean navigationLocked = false;

	public DesktopUserPanel() {
		super(TABBAR_HEIGHT, Style.Unit.PX);
		setSize("100%", "100%");
	}

	@Override
	public void setPresenter(UserPanelPresenter presenter) {
		this.presenter = presenter;

	}

	@Override
	public void showTab(int index) {
		if (!handlersInitialized) {
			addSelectionHandler(new SelectionHandler<Integer>() {

				@Override
				public void onSelection(SelectionEvent<Integer> event) {
					presenter.tabSelected(event.getSelectedItem());
				}
			});

			addBeforeSelectionHandler(new BeforeSelectionHandler<Integer>() {

				@Override
				public void onBeforeSelection(
						BeforeSelectionEvent<Integer> event) {
					if (getSelectedTab() >= 0) {
						presenter.tabDeselected(getSelectedTab());
					}
				}

			});

			handlersInitialized = true;
		}

		selectTab(index);
	}

	@Override
	public void attach(Widget w, String label) {
		add(w, label, true);
		setTabStyle(w);
	}

	@Override
	public void replaceWidget(int index, String tabText, Widget w) {
		if (getWidgetCount() > index + 1) {
			remove(index);
		}
		insertWidget(index, tabText, w);
	}

	@Override
	public void detachWidget(Widget w) {
		remove(w);
	}

	@Override
	public void highlight(int index, boolean highlight, String image) {
		String html = ((Label) getTabWidget(index)).getText();
		if (html == null) {
			return;
		}

		int i = html.indexOf("<img");
		if (i < 0) {
			i = html.indexOf("<IMG");
		}

		if (i >= 0) {
			html = html.substring(0, i).trim();
		}

		String newHtml = html;
		if (highlight) {
			newHtml = html + " <img src='" + image
					+ "' border='0' align='middle'>";
		}

		setTabHTML(index, newHtml);
	}

	@Override
	public void insertWidget(int index, String text, Widget w) {
		insert(w, text, index);
		setTabStyle(w);
	}

	@Override
	public void selectTab(int index) {
		if (!navigationLocked) {
			super.selectTab(index);
		}
	}

	private void setTabStyle(Widget w) {
		int index = getWidgetIndex(w);
		if (index == -1) {
			return;
		}

		((Label) getTabWidget(index)).getParent().getElement().getStyle()
				.setFloat(Style.Float.LEFT);
	}

	@Override
	public int getSelectedTab() {
		return getSelectedIndex();
	}

	@Override
	public void lockEnabled(boolean enabled) {
		navigationLocked = enabled;
	}

}
