package com.quikj.ace.migration.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.quikj.client.raccess.RemoteAccessClient;
import com.quikj.server.framework.AceConfigFileHelper;
import com.quikj.server.framework.AceLogger;

/**
 * This servlet is used for migrating existing Ace Operator version 1 customers
 * to version 2. It reroutes received V1 requests (of the form
 * http://.../servlet/ContactCenterServlet?customer=xyz) to the configured
 * operator available/busy URLs specified in
 * .ace/profiles/redirect/xyz.properties.
 */
public class VisitorRedirectServlet extends HttpServlet {

	private static final long serialVersionUID = 1583174329620312623L;
	private static final String FILE_LAST_READ_TIME = "fileLastModifiedTime";

	private static Hashtable<String, Properties> profileProperties = new Hashtable<String, Properties>();

	private static final String DEFAULT_URL = "/ace-custom/groups/${customer}/chat.html?profile=${profile}&locale=${locale}";

	public static final String PROPERTY_GROUP = "group";
	public static final String PROPERTY_LOCALE = "locale";
	public static final String PROPERTY_PROFILE = "profile";
	public static final String PROPERTY_URL_AVAILABLE = "operatorAvailableUrl";
	public static final String PROPERTY_URL_BUSY = "operatorBusyUrl";

	private static final String[] SERVER_PROPERTIES = { PROPERTY_GROUP,
			PROPERTY_URL_AVAILABLE, PROPERTY_URL_BUSY, PROPERTY_LOCALE,
			PROPERTY_PROFILE };

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);

		String logReroutes = servletConfig.getInitParameter("rerouteLogging");
		getServletContext().setAttribute("ace-migration.logReroutes",
				logReroutes);
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException {

		boolean logReroutes = false;
		String logging = (String) getServletContext().getAttribute(
				"ace-migration.logReroutes");
		if (logging != null && logging.equalsIgnoreCase("true")) {
			logReroutes = true;
		}

		String customer = request.getParameter("customer");
		if (customer == null) {
			throw new ServletException(
					"The 'customer' parameter is not specified");
		}

		try {
			String file = AceConfigFileHelper.getAcePath("profiles"
					+ File.separator + "redirect", customer + ".properties");
			File f = new File(file);
			Properties properties = profileProperties.get(customer);
			if (properties == null) {
				properties = initProperties(customer, f);
			} else {
				long lastRead = Long.parseLong(properties
						.getProperty(FILE_LAST_READ_TIME));
				if (lastRead < f.lastModified()) {
					// The file has changed since it was read last time
					profileProperties.remove(customer);
					properties = initProperties(customer, f);
				}
			}

			String group = properties.getProperty(PROPERTY_GROUP);
			if (group == null) {
				throw new ServletException(
						"The profile does not contain a 'group' property");
			}

			boolean busy = false;
			try {
				busy = operatorBusy(group);
			} catch (NotBoundException e) {
				e.printStackTrace();
				busy = true;
			}

			String locale = getLocale(request, properties);

			String url = getUrl(request, properties, customer, busy, locale);
			if (logReroutes) {
				AceLogger.Instance().log(
						AceLogger.INFORMATIONAL,
						AceLogger.SYSTEM_LOG,
						"Redirecting customer " + customer + " to url: " + url
								+ ", busy: " + busy + ", locale: " + locale);
			}

			redirect(request, response, url);

			return;

		} catch (Exception e) {
			AceLogger.Instance().log(
					AceLogger.ERROR,
					AceLogger.SYSTEM_LOG,
					"Error rerouting V1 URL for customer = " + customer + " - "
							+ e.getClass().getName() + " : " + e.getMessage());

			e.printStackTrace();
			throw new ServletException(e);
		}
	}

	private String getLocale(HttpServletRequest request, Properties properties) {
		String locale = properties.getProperty(PROPERTY_LOCALE);

		String urlLocale = request.getParameter(PROPERTY_LOCALE);
		if (urlLocale != null) {
			locale = urlLocale;
		}

		return locale;
	}

	private boolean operatorBusy(String group) throws RemoteException,
			MalformedURLException, NotBoundException {
		String val = getParam(
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

	private String getParam(String object, String paramName)
			throws RemoteException, MalformedURLException, NotBoundException {
		RemoteAccessClient rmi = (RemoteAccessClient) getServletContext()
				.getAttribute("ace-migration.remoteAccess");
		if (rmi == null) {
			return null;
		}
		
		return rmi.getRemoteAccess().getParam(object, paramName);
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

	private void redirect(HttpServletRequest request,
			HttpServletResponse response, String url) throws IOException {

		URL redirectTo = null;

		if (url.startsWith("http")) {
			redirectTo = new URL(url);
		} else {
			URL reqUrl = new URL(request.getRequestURL().toString());
			String port = reqUrl.getPort() <= 0 ? "" : (":" + Integer
					.toString(reqUrl.getPort()));
			redirectTo = new URL(reqUrl.getProtocol() + "://"
					+ reqUrl.getHost() + port + url);
		}

		String fullUrl = response.encodeRedirectURL(redirectTo.toString());

		response.sendRedirect(fullUrl);
	}

	private String getUrl(HttpServletRequest request, Properties properties,
			String customer, boolean busy, String locale) {

		String url = null;
		if (busy) {
			url = properties.getProperty(PROPERTY_URL_BUSY);
			if (url == null) {
				url = DEFAULT_URL;
			}
		} else {
			url = properties.getProperty(PROPERTY_URL_AVAILABLE);
			if (url == null) {
				url = DEFAULT_URL;
			}
		}

		String profile = properties.getProperty(PROPERTY_PROFILE);
		if (profile == null) {
			profile = customer;
		}

		url = url.replace("${profile}", profile);
		url = url.replace("${customer}", customer);

		if (locale != null) {
			url = url.replace("${locale}", locale);
		}

		if (url.startsWith("/") || url.startsWith("http")) {
			return url;
		}

		// It's a relative URL
		return request.getContextPath() + "/" + url;
	}
}
