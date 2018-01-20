/**
 * 
 */
package com.quikj.ace.web.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.gwt.user.client.Window;
import com.quikj.ace.web.client.presenter.MainPanelPresenter;

/**
 * @author amit
 * 
 */
public class ClientProperties extends HashMap<String, List<String>> {

	private static final long serialVersionUID = 2342521599756649778L;

	public static final String FAST_POLL_TIMER = "fastPollTimer";

	public static final String APP_ID = "appId";

	public static final String THEME = "theme";

	public static final String ROOT_PANEL = "rootPanelId";

	public static final String PROFILE = "profile";

	public static final String LOCALE = "locale";

	public static final String TYPE = "type";

	public static final String TRANSCRIPT_EMAIL_TO = "transcriptEmailTo";

	public static final String TRANSCRIPT_EMAIL_FROM = "transcriptEmailFrom";

	public static final String EMAIL_TRANSCRIPT = "emailTranscript";

	public static final String ALL_OPERATOR_BUSY_URL = "allOperatorBusyUrl";

	public static final String ALL_OPERATOR_BUSY_EMAIL = "allOperatorBusyEmail";

	public static final String GROUP = "group";

	public static final String VISITOR_EMAIL_MANDATORY = "visitorEmailMandatory";

	public static final String VISITOR_EMAIL_HIDDEN = "visitorEmailHidden";

	public static final String VISITOR_FORM_CAPTION = "visitorFormCaption";

	public static final String VISITOR_CHAT_ENDED_URL = "visitorChatEndedUrl";
	
	public static final String VISITOR_CHAT_ENDED_HTML = "visitorChatEndedHtml";

	public static final String SHOW_OTHER_PARTY_DETAILS = "showOtherPartyDetails";
	
	public static final String HIDE_OTHER_PARTY_COOKIE_IPADDRESS = "hideOtherPartyCookieIpAddress";

	public static final String HIDE_LOGIN_IDS = "hideLoginIds";

	public static final String VERBOSE_MESSAGES = "verboseMessages";

	public static final String MAX_LIST_IDLE_CHATS = "idleChatListLimit";

	public static final String MAX_RECONNECT_RETRY = "maxReconnectRetry";

	public static final String AUDIO_CHIME = "audioChime";

	public static final String COOKIE_IN_TRANSCRIPT_SUBJECT = "cookieInTranscriptSubject";

	public static final String DATE_TIME_FORMAT = "dateTimeFormat";

	public static final String DATE_FORMAT = "dateFormat";

	public static final String TIME_FORMAT = "timeFormat";

	public static final String USER_NAME = "userName";

	public static final String USER_EMAIL = "userEmail";

	public static final String USER_ADDITIONAL_INFO = "userAdditionalInfo";

	public static final String SUPPRESS_COPY_PASTE = "suppressCopyPaste";

	public static final String LEGACY_MEDIA_PLAYER = "legacyMediaPlayer";

	public static final String VISITOR_CHAT_DECLINED_URL = "visitorChatDeclinedUrl";

	public static final String ONCLICK_START_PAGE = "startPage";

	public static final String CONVERSATION_SMALL_SPACE = "smallSpace";
	
	public static final String REMOVE_MOBILE_BORDER = "removeMobileBorder";

	public static final String DISPLAY_FILE_SHARE = "displayFileShare";
	
	public static final String GET_CANNED_MESSAGE_CONTENT = "getCannedMessageContent";
	
	public static final String CURRENCY_DELIMITER = "currencyDelimiter";
	
	public static final String RECAPTCHA2_SITE_KEY = "recaptcha2SiteKey";
	
	// not properties, just a constants
	public static final String OPERATOR_TYPE = "operator";
	public static final String VISITOR_TYPE = "visitor";

	public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
	public static final String DEFAULT_TIME_FORMAT = "HH:mm";

	private static ClientProperties instance = null;

	private static final String[] ALLOWED_URL_PROPERTIES = { LOCALE, THEME,
			PROFILE, ONCLICK_START_PAGE };

	private ClientProperties() {
		super();
	}

	public static ClientProperties getInstance() {
		if (instance == null) {
			instance = new ClientProperties();
		}

		return instance;
	}

	public int getIntValue(String name, int defaultValue) {
		String value = getStringValue(name, Integer.toString(defaultValue));
		return Integer.parseInt(value);
	}

	public String getStringValue(String name, String defaultValue) {
		List<String> values = get(name);
		String value = null;
		if (values == null || values.size() == 0) {
			value = defaultValue;
		} else {
			value = values.get(0);
		}

		return value;
	}

	public boolean getBooleanValue(String name, boolean defaultValue) {
		List<String> values = get(name);
		boolean ret = false;
		if (values == null || values.size() == 0) {
			ret = defaultValue;
		} else if (values.get(0).equals("true")) {
			ret = true;
		} else if (values.get(0).equals("false")) {
			ret = false;
		}

		return ret;
	}

	// TODO add parameter validation
	public boolean loadProperties(String browserType, HashMap<String, String> properties) {
		if (properties != null) {
			for (String name : properties.keySet()) {
				String values = properties.get(name);
				List<String> list = new ArrayList<String>();
				list.add(values);
				put(name, list);
			}
		}

		if (browserType.equals(MainPanelPresenter.BROWSER_MOBILE)) {
			String type = getStringValue(ClientProperties.TYPE, OPERATOR_TYPE);
			if (!type.equals(VISITOR_TYPE)) {
				Window.alert(ApplicationController.getMessages()
						.ClientProperties_operatorLoginMobile());
				return false;
			}
		}

		Map<String, List<String>> paramMap = Window.Location.getParameterMap();
		for (Map.Entry<String, List<String>> e : paramMap.entrySet()) {
			String key = e.getKey();

			if (!propertyAllowed(key)) {
				Logger.getLogger(getClass().getName())
						.warning(
								"The parameter "
										+ key
										+ " specified as the URL parameter is not allowed."
										+ " Going to ignore the parameter");
				continue;
			}

			List<String> values = e.getValue();
			put(key, values);

			Logger.getLogger(getClass().getName()).finest(
					"Loading property: key = " + key + ", size = "
							+ values.size() + ", first value = "
							+ values.get(0));
		}

		return true;
	}

	private boolean propertyAllowed(String propertyName) {
		for (String allowedProp : ALLOWED_URL_PROPERTIES) {
			if (allowedProp.equals(propertyName)) {
				return true;
			}
		}

		if (propertyName.startsWith("user")) {
			return true;
		}

		return false;
	}
}
