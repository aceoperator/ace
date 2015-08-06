package com.quikj.ace.web.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.quikj.server.app.adapter.AppServerAdapterException;
import com.quikj.server.app.adapter.PolledAppServerAdapter;
import com.quikj.server.framework.AceConfigFileHelper;

public class IconServlet extends HttpServlet {

	private static final String FILE_LAST_READ_TIME = "fileLastModifiedTime";

	private static final long serialVersionUID = 9120211725683240148L;

	private static Hashtable<String, Properties> profileProperties = new Hashtable<String, Properties>();

	private static final String DEFAULT_OPERATOR_AVAILABLE_HTML = "<IMG SRC='${context}/icons/huge/receptionist_128.png'>";
	private static final String DEFAULT_OPERATOR_BUSY_HTML = "<IMG SRC='${context}/icons/huge/kmail.png'>";
	private static final String DEFAULT_ON_CLICK_AVAILABLE = "window.open('/ace-contactcenter/Ace_web.html?profile=${profile}','', 'scrollbars=0,menubar=0,height=650,width=600,resizable=1,toolbar=0,location=0,status=0'); return false;";
	private static final String DEFAULT_ON_CLICK_BUSY = "window.open('/ace-contactcenter/Ace_web.html?profile=${profile}&startPage=busy','', 'scrollbars=0,menubar=0,height=650,width=600,resizable=1,toolbar=0,location=0,status=0'); return false;";
	private static final String DEFAULT_ICON_PAGE_PREFIX = "<html><head></head><body>";
	private static final String DEFAULT_ICON_PAGE_SUFFIX = "</body></html>";

	private static final String[] SERVER_PROPERTIES = {
			ServerProperties.PROPERTY_GROUP, ServerProperties.PROPERTY_LOCALE,
			ServerProperties.PROPERTY_OPERATOR_AVAILABLE_HTML,
			ServerProperties.PROPERTY_OPERATOR_BUSY_HTML,
			ServerProperties.PROPERTY_ONCLICK_AVAILABLE,
			ServerProperties.PROPERTY_ONCLICK_BUSY,
			ServerProperties.PROPERTY_HREF_AVAILABLE,
			ServerProperties.PROPERTY_HREF_BUSY,
			ServerProperties.PROPERTY_ICON_PAGE_PREFIX,
			ServerProperties.PROPERTY_ICON_PAGE_SUFFIX };

	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException {
		try {
			String profile = request.getParameter("profile");
			if (profile == null) {
				throw new ServletException(
						"The 'profile' parameter is not specified");
			}

			String browser = request.getParameter("browser");
			if (browser != null && !browser.equals("desktop")) {
				profile = profile + "-" + browser;
			}

			String file = AceConfigFileHelper.getAcePath("profiles", profile
					+ ".properties");
			File f = new File(file);
			if (!f.exists()) {
				profile = request.getParameter("profile");
				file = AceConfigFileHelper.getAcePath("profiles", profile
						+ ".properties");
				f = new File(file);
			}

			Properties properties = profileProperties.get(profile);
			if (properties == null) {
				properties = initProperties(profile, f);
			} else {
				long lastRead = Long.parseLong(properties
						.getProperty(FILE_LAST_READ_TIME));
				if (lastRead < f.lastModified()) {
					// The file has changed since it was read last time
					profileProperties.remove(profile);
					properties = initProperties(profile, f);
				}
			}

			String group = properties
					.getProperty(ServerProperties.PROPERTY_GROUP);
			if (group == null) {
				throw new ServletException(
						"The profile does not contain a 'group' property");
			}

			boolean busy = false;
			try {
				busy = operatorBusy(group);
			} catch (AppServerAdapterException e) {
				e.printStackTrace();
				busy = true;
			}

			String html = getLinkValue(properties, busy);
			String href = getHref(properties, busy);
			String onclick = null;
			if (href == null) {
				onclick = getOnClick(properties, busy);
			}

			html = formatHtml(properties, html, onclick, href);
			html = html.replace("${profile}", profile);
			html = html.replace("${context}", request.getContextPath());

			String locale = getLocale(request, properties);
			if (locale != null) {
				html = html.replace("${locale}", locale);
			}

			outputPage(response, html);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException(e);
		}
	}

	private String getLocale(HttpServletRequest request, Properties properties) {
		String locale = properties
				.getProperty(ServerProperties.PROPERTY_LOCALE);

		String urlLocale = request
				.getParameter(ServerProperties.PROPERTY_LOCALE);
		if (urlLocale != null) {
			locale = urlLocale;
		}

		return locale;
	}

	private String getOnClick(Properties properties, boolean busy) {

		String onclick = null;
		if (busy) {
			onclick = properties
					.getProperty(ServerProperties.PROPERTY_ONCLICK_BUSY);
			if (onclick == null) {
				onclick = DEFAULT_ON_CLICK_BUSY;
			}
		} else {
			onclick = properties
					.getProperty(ServerProperties.PROPERTY_ONCLICK_AVAILABLE);
			if (onclick == null) {
				onclick = DEFAULT_ON_CLICK_AVAILABLE;
			}
		}

		return onclick;
	}

	private String getHref(Properties properties, boolean busy) {

		String href = null;
		if (busy) {
			href = properties.getProperty(ServerProperties.PROPERTY_HREF_BUSY);
		} else {
			href = properties
					.getProperty(ServerProperties.PROPERTY_HREF_AVAILABLE);
		}

		return href;
	}

	private String getLinkValue(Properties properties, boolean busy) {

		String html = null;
		if (busy) {
			html = properties
					.getProperty(ServerProperties.PROPERTY_OPERATOR_BUSY_HTML);
			if (html == null) {
				html = DEFAULT_OPERATOR_BUSY_HTML;
			}
		} else {
			html = properties
					.getProperty(ServerProperties.PROPERTY_OPERATOR_AVAILABLE_HTML);
			if (html == null) {
				html = DEFAULT_OPERATOR_AVAILABLE_HTML;
			}
		}

		return html;
	}

	private String formatHtml(Properties properties, String linkValue,
			String onclick, String href) {
		String prefix = properties
				.getProperty(ServerProperties.PROPERTY_ICON_PAGE_PREFIX);
		if (prefix == null) {
			prefix = DEFAULT_ICON_PAGE_PREFIX;
		}

		String suffix = properties
				.getProperty(ServerProperties.PROPERTY_ICON_PAGE_SUFFIX);
		if (suffix == null) {
			suffix = DEFAULT_ICON_PAGE_SUFFIX;
		}

		StringBuilder html = new StringBuilder();
		html.append(prefix);
		html.append("<a href='");
		if (href != null) {
			html.append(href);
		} else {
			html.append("#");
		}
		html.append("'");

		if (onclick != null) {
			html.append(" onclick=\"");
			html.append(onclick);
			html.append("\"");
		}
		
		if (href != null) {
			html.append(" target='_parent'");
		}

		html.append(">");
		html.append(linkValue);
		html.append("</a>");
		html.append(suffix);
		return html.toString();
	}

	private void outputPage(HttpServletResponse response, String html)
			throws IOException {

		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Pragma", "no-cache");
		response.setDateHeader("Expires", 0);

		ServletOutputStream ros = response.getOutputStream();

		ros.print(html);

		ros.flush();
		ros.close();
	}

	private boolean operatorBusy(String group) throws AppServerAdapterException {
		String val = PolledAppServerAdapter.getInstance().getParam(
				"com.quikj.application.web.talk.feature.operator.Operator:"
						+ group, "all-operators-busy");
		if (val == null) {
			return true;
		} else if (val.equals("false")) {
			return false;
		} else {
			return true;
		}
	}

	private Properties initProperties(String profile, File propFile)
			throws IOException {
		FileInputStream fis = new FileInputStream(propFile);
		Properties p = new Properties();
		p.load(fis);
		fis.close();

		Properties ret = new Properties();
		for (String sprop : SERVER_PROPERTIES) {
			String val = p.getProperty(sprop);
			if (val != null) {
				ret.put(sprop, val);
			}
		}
		ret.setProperty(FILE_LAST_READ_TIME,
				Long.toString(System.currentTimeMillis()));

		profileProperties.put(profile, ret);
		return ret;
	}
}
