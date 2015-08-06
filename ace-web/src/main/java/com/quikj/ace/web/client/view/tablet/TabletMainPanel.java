/**
 * 
 */
package com.quikj.ace.web.client.view.tablet;

import com.quikj.ace.web.client.RootPanelResizeListener;
import com.quikj.ace.web.client.presenter.MainPanelPresenter;
import com.quikj.ace.web.client.view.AbstractMainPanel;
import com.quikj.ace.web.client.view.MainPanel;

/**
 * @author amit
 * 
 */
public class TabletMainPanel extends AbstractMainPanel implements MainPanel,
		RootPanelResizeListener {

	public TabletMainPanel() {
		super();
	}

	@Override
	public String browserType() {
		return MainPanelPresenter.BROWSER_TABLET;
	}
}
