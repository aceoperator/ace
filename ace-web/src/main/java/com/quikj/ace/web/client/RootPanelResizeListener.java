/**
 * 
 */
package com.quikj.ace.web.client;

/**
 * @author amit
 *
 */
public interface RootPanelResizeListener {
	
	public enum Layout {
		POTRAIT,
		LANDSCAPE
	}
	
	public void onResize (int width, int height, Layout layout);
}
