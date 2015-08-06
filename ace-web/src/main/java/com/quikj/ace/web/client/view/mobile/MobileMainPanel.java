/**
 * 
 */
package com.quikj.ace.web.client.view.mobile;

import com.quikj.ace.web.client.RootPanelResizeListener;
import com.quikj.ace.web.client.presenter.MainPanelPresenter;
import com.quikj.ace.web.client.view.AbstractMainPanel;
import com.quikj.ace.web.client.view.MainPanel;

/**
 * @author amit
 * 
 */
public class MobileMainPanel extends AbstractMainPanel implements MainPanel,
		RootPanelResizeListener {

	public MobileMainPanel() {
		super();
	}

	@Override
	public String browserType() {
		return MainPanelPresenter.BROWSER_MOBILE;
	}

}
