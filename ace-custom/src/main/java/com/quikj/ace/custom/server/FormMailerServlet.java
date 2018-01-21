/**
 * 
 */
package com.quikj.ace.custom.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.quikj.server.framework.AceMailMessage;
import com.quikj.server.framework.AceMailService;
import com.quikj.server.framework.CaptchaService;
import com.quikj.server.framework.CaptchaService.CaptchaType;

/**
 * @author amit
 * 
 */
public class FormMailerServlet extends HttpServlet {

	private static final long serialVersionUID = -2391000119541064116L;

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		ResourceBundle messages = ResourceBundle.getBundle(
				"com.quikj.ace.custom.server.CustomResources",
				getLocale(request));

		String group = request.getParameter("group");
		if (group == null) {
			throw new ServletException(
					"The mandatory parameter 'group' has not been specified");
		}

		String captcha = request.getParameter("captcha");
		if (captcha == null) {
			throw new ServletException(
					"The mandatory parameter 'captcha' has not been specified");
		}

		if (captcha.trim().isEmpty()) {
			showResult(request, response,
					messages.getString("imagePasscodeMissing"));
			return;
		}
		
		try {
			captcha = captcha.trim();
			boolean correct = CaptchaService.getInstance().verify(request.getSession().getId(),  
							captcha, request.getRemoteAddr(), CaptchaType.IMAGE);
			if (!correct) {
				showResult(request, response,
						messages.getString("unmatchedCapchaChars"));
				return;
			}
		} catch (Exception e) {
			// should not happen, may be thrown if the id is not valid
			e.printStackTrace();
			showResult(request, response, messages.getString("tryImageAgain"));
			return;
		}

		String form = request.getParameter("form");
		if (form == null) {
			form = "default";
		}

		String path = request.getServletContext().getRealPath(
				"groups/" + group + "/form_" + form + ".properties");
		FileInputStream fis = new FileInputStream(path);
		Properties props = new Properties();
		props.load(fis);
		fis.close();

		String to = props.getProperty("to");
		if (to == null) {
			throw new ServletException("The 'to' property is not set");
		}

		// Everything checks out, lets process the mail
		AceMailMessage msg = new AceMailMessage();

		String from = props.getProperty("from");
		if (from != null) {
			msg.setFrom(from);
		}

		Vector<String> rec = new Vector<String>();
		msg.setTo(rec);

		String[] recipients = to.split(",");
		for (String recipient : recipients) {
			rec.add(recipient);
		}

		String subject = props.getProperty("subject");
		if (subject == null) {
			subject = "Feedback received from web user for form " + form;
		}
		msg.setSubject(subject);

		String cc = props.getProperty("cc");
		if (cc != null) {
			Vector<String> cclist = new Vector<String>();
			msg.setCc(cclist);

			String[] ccUsers = cc.split(";");
			for (String ccUser : ccUsers) {
				cclist.add(ccUser);
			}
		}

		String bcc = props.getProperty("bcc");
		if (bcc != null) {
			Vector<String> bcclist = new Vector<String>();
			msg.setBcc(bcclist);

			String[] bccUsers = bcc.split(";");
			for (String bccUser : bccUsers) {
				bcclist.add(bccUser);
			}
		}

		String body = formatBody(form, request);
		msg.setBody(body);

		AceMailService.getInstance().addToMailQueue(msg);
		showResult(request, response, null);
	}

	private Locale getLocale(HttpServletRequest request) {

		Locale locale = request.getLocale();
		String override = request.getParameter("locale");
		if (override != null) {
			String[] args = override.split("_");
			String language = args[0];
			String country = args.length > 1 ? args[1] : "";
			locale = new Locale(language, country);
		}

		return locale;
	}

	private String formatBody(String form, HttpServletRequest request) {

		Enumeration<String> params = request.getParameterNames();
		List<String> sortedList = new ArrayList<String>();
		while (params.hasMoreElements()) {
			sortedList.add(params.nextElement());
		}
		Collections.sort(sortedList);
		
		StringBuilder buffer = new StringBuilder();
		buffer.append("The following information was submitted\n\n");
		for (String param: sortedList) {
			String value = request.getParameter(param);
			if (value != null && value.trim().length() > 0) {
				buffer.append(param);
				buffer.append(" : ");
				buffer.append(value);
				buffer.append("\n");
			}
		}
		return buffer.toString();
	}

	private void showResult(HttpServletRequest request,
			HttpServletResponse response, String error)
			throws ServletException, IOException {
		if (error != null) {
			request.setAttribute("error", error);
		}

		request.getRequestDispatcher("/form_result.jsp").forward(request,
				response);
	}
}
