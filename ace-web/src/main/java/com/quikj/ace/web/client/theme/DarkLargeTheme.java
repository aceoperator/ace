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
public interface DarkLargeTheme extends ClientBundle {
	
	public static final DarkLargeTheme INSTANCE = GWT.create(DarkLargeTheme.class);

	@Source("com/quikj/ace/web/client/theme/DarkLarge.css")
	public DarkLarge darkLarge();
	
	@Source("com/quikj/ace/web/client/theme/Dark-images/hborder.png")
	public DataResource hborderUrl();
	
	@Source("com/quikj/ace/web/client/theme/Dark-images/hborder_ie6.png")
	public DataResource hborderIe6Url();
	
	@Source("com/quikj/ace/web/client/theme/Dark-images/vborder.png")
	public DataResource vborderUrl();
	
	@Source("com/quikj/ace/web/client/theme/Dark-images/vborder_ie6.png")
	public DataResource vborderIe6Url();
	
	@Source("com/quikj/ace/web/client/theme/Dark-images/corner.png")
	public DataResource cornerUrl();
	
	@Source("com/quikj/ace/web/client/theme/Dark-images/corner_ie6.png")
	public DataResource cornerIe6Url();
	
	@Source("com/quikj/ace/web/client/theme/Dark-images/splitPanelThumb.png")
	public DataResource splitPanelThumb();
}
