/**
 * 
 */
package com.quikj.ace.web.client.view.desktop;

import com.quikj.ace.web.client.RootPanelResizeListener;
import com.quikj.ace.web.client.presenter.MainPanelPresenter;
import com.quikj.ace.web.client.view.AbstractMainPanel;
import com.quikj.ace.web.client.view.MainPanel;

/**
 * @author amit
 * 
 */
public class DesktopMainPanel extends AbstractMainPanel implements MainPanel,
		RootPanelResizeListener {

	public DesktopMainPanel() {
		super();
	}

	@Override
	public String browserType() {
		return MainPanelPresenter.BROWSER_DESKTOP;
	}
}
