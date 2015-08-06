/**
 * 
 */
package com.quikj.ace.web.client.theme;

import java.util.ArrayList;
import java.util.List;

import com.quikj.ace.web.client.ClientProperties;

/**
 * @author amit
 * 
 */
public class ThemeFactory {

	private static String selectedTheme = null;
	
	private enum Theme {
		CHROME("chrome"),
		CHROME_SMALL("chrome-small"),
		CHROME_LARGE("chrome-large"),
		DARK("dark"),
		DARK_SMALL("dark-small"),
		DARK_LARGE("dark-large");
		
		private final String value;
		
		Theme(String value) {
			this.value = value;
		}
		
		public String value() {
			return value;
		}
		
		public static Theme toTheme(String value) {
			for (Theme theme: Theme.values()) {
				if (theme.value().equals(value)) {
					return theme;
				}
			}
			
			return null;
		}
	}
	
	public static void initTheme(String theme) {
		initTheme(Theme.toTheme(theme));
	}
	
	public static void initTheme(Theme theme) {
		if (theme == null) {
			selectedTheme = null;
			return;
		}
		
		switch(theme) {
		case CHROME:
			ClientTheme.setChrome();
			break;
			
		case CHROME_SMALL:
			ClientTheme.setChromeSmall();
			break;
			
		case CHROME_LARGE:
			ClientTheme.setChromeLarge();
			break;
			
		case DARK:
			ClientTheme.setDark();
			break;
			
		case DARK_SMALL:
			ClientTheme.setDarkSmall();
			break;
			
		case DARK_LARGE:
			ClientTheme.setDarkLarge();
			break;
		}
		
		selectedTheme = theme.value();
	}
	
	public static void initTheme() {
		String theme = ClientProperties.getInstance().getStringValue(
				ClientProperties.THEME, null);
		initTheme(Theme.toTheme(theme));
	}
	
	public static List<String> listThemes() {
		List<String> themes = new ArrayList<String>();
		for (Theme theme: Theme.values()) {
			themes.add(theme.value());
		}
		return themes;	
	}

	public static String getSelectedTheme() {
		return selectedTheme;
	}
}
