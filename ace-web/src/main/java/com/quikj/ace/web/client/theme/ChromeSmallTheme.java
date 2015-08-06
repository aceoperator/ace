/**
 * 
 */
package com.quikj.ace.web.client.theme;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.DataResource;

/**
 * @author beckie
 *
 */
public interface ChromeSmallTheme extends ClientBundle {
	
	public static final ChromeSmallTheme INSTANCE = GWT.create(ChromeSmallTheme.class);

	@Source("com/quikj/ace/web/client/theme/ChromeSmall.css")
	public ChromeSmall chromeSmall();
	
	@Source("com/quikj/ace/web/client/theme/Chrome-images/hborder.png")
	public DataResource hborderUrl();
	
	@Source("com/quikj/ace/web/client/theme/Chrome-images/hborder_ie6.png")
	public DataResource hborderIe6Url();
	
	@Source("com/quikj/ace/web/client/theme/Chrome-images/vborder.png")
	public DataResource vborderUrl();
	
	@Source("com/quikj/ace/web/client/theme/Chrome-images/vborder_ie6.png")
	public DataResource vborderIe6Url();
	
	@Source("com/quikj/ace/web/client/theme/Chrome-images/corner.png")
	public DataResource cornerUrl();
	
	@Source("com/quikj/ace/web/client/theme/Chrome-images/corner_ie6.png")
	public DataResource cornerIe6Url();
	
	@Source("com/quikj/ace/web/client/theme/Chrome-images/splitPanelThumb.png")
	public DataResource splitPanelThumb();
}
