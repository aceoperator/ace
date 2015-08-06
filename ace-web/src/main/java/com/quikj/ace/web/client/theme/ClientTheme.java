/**
 * 
 */
package com.quikj.ace.web.client.theme;



/**
 * @author beckie
 *
 */
public class ClientTheme {

	public static void setChrome() {
		ChromeTheme.INSTANCE.chrome().ensureInjected();
	}
	
	public static void setChromeSmall() {
		ChromeSmallTheme.INSTANCE.chromeSmall().ensureInjected();
	}
	
	public static void setChromeLarge() {
		ChromeLargeTheme.INSTANCE.chromeLarge().ensureInjected();
	}
	
	public static void setChromeRtl() {
		ChromeRTLTheme.INSTANCE.chromeRTL().ensureInjected();
	}
	
	public static void setDark() {
		DarkTheme.INSTANCE.dark().ensureInjected();
	}
	
	public static void setDarkSmall() {
		DarkSmallTheme.INSTANCE.darkSmall().ensureInjected();
	}
	
	public static void setDarkLarge() {
		DarkLargeTheme.INSTANCE.darkLarge().ensureInjected();
	}
	
	public static void setDarkRtl() {
		DarkRTLTheme.INSTANCE.darkRTL().ensureInjected();
	}
}
