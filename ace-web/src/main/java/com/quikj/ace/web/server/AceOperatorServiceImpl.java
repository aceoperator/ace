package com.quikj.ace.web.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import javax.servlet.http.Cookie;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.quikj.ace.messages.vo.adapter.GroupInfo;
import com.quikj.ace.messages.vo.app.Message;
import com.quikj.ace.messages.vo.talk.CannedMessageElement;
import com.quikj.ace.messages.vo.talk.MailElement;
import com.quikj.ace.messages.vo.talk.SendMailRequestMessage;
import com.quikj.ace.web.client.AceOperatorService;
import com.quikj.ace.web.shared.AceServerException;
import com.quikj.application.web.talk.plugin.accounting.OpBusyCDR;
import com.quikj.server.app.adapter.AppServerAdapterException;
import com.quikj.server.app.adapter.PolledAppServerAdapter;
import com.quikj.server.framework.AceMailMessage;
import com.quikj.server.framework.AceMailService;
import com.quikj.server.framework.CaptchaService;

public class AceOperatorServiceImpl extends RemoteServiceServlet implements
		AceOperatorService {

	private static final long serialVersionUID = -911958659581871687L;

	private static final int ACE_ENDUSER_COOKIE_AGE = 365 * 24 * 3600;
	private Long cookieSeed = 0L;

	@Override
	public List<Message> exchangeMessages(Message incoming)
			throws AceServerException {
		try {
			return PolledAppServerAdapter.getInstance().exchangeMessages(
					incoming);
		} catch (AppServerAdapterException e) {
			e.printStackTrace();
			throw formatServerException(e);
		}
	}

	@Override
	public List<Message> exchangeMessages(List<Message> incoming)
			throws AceServerException {
		List<Message> ret = new ArrayList<Message>();
		AceServerException exceptionThrown = null;
		for (Message message : incoming) {
			try {
				List<Message> out = exchangeMessages(message);
				if (out != null && out.size() > 0) {
					ret.addAll(out);
				}
			} catch (AceServerException e) {
				if (!e.isRecoverable()) {
					throw e;
				}

				exceptionThrown = e;
			}
		}

		if (ret.size() == 0 && exceptionThrown != null) {
			throw exceptionThrown;
		}

		return ret;
	}

	@Override
	public String connect() throws AceServerException {
		try {
			String ip = getThreadLocalRequest().getRemoteAddr();
			Cookie cookie = manageCookie(ip);
			return PolledAppServerAdapter.getInstance().clientConnected(ip,
					cookie.getValue());
		} catch (AppServerAdapterException e) {
			e.printStackTrace();
			throw formatServerException(e);
		}
	}

	private Cookie manageCookie(String ip) {
		Cookie cookie = null;
		Cookie[] cookies = getThreadLocalRequest().getCookies();
		if (cookies != null) {
			for (Cookie c : cookies) {
				if (c.getName().equals(AceOperatorService.ACE_ENDUSER_COOKIE_NAME)) {
					cookie = c;
					break;
				}
			}
		}

		if (cookie == null) {
			cookie = new Cookie(AceOperatorService.ACE_ENDUSER_COOKIE_NAME, generateCookieValue());
			cookie.setMaxAge(ACE_ENDUSER_COOKIE_AGE);
			getThreadLocalResponse().addCookie(cookie);
		}

		return cookie;
	}

	private String generateCookieValue() {
		synchronized (cookieSeed) {
			return "ep_" + new Date().getTime() + "_" + cookieSeed++;
		}
	}

	@Override
	public void disconnect(String sessionId) throws AceServerException {
		try {
			PolledAppServerAdapter.getInstance().clientDisconnected(sessionId);
		} catch (AppServerAdapterException e) {
			e.printStackTrace();
			throw formatServerException(e);
		}
	}

	@Override
	public CannedMessageElement[] listCannedMessages(String[] groups, boolean fetchContent)
			throws AceServerException {
		try {
			return PolledAppServerAdapter.getInstance().listCannedMessages(
					groups, fetchContent);
		} catch (AppServerAdapterException e) {
			e.printStackTrace();
			throw formatServerException(e);
		}
	}

	@Override
	public HashMap<String, String> getProfile(String profileName,
			String browserType) throws AceServerException {
		try {
			Properties properties = PolledAppServerAdapter.getInstance()
					.getProfile(profileName, browserType);
			HashMap<String, String> ret = new HashMap<String, String>();
			for (Entry<Object, Object> prop : properties.entrySet()) {
				ret.put((String) prop.getKey(), (String) prop.getValue());
			}
			return ret;
		} catch (AppServerAdapterException e) {
			e.printStackTrace();
			throw formatServerException(e);
		}
	}

	@Override
	public boolean allOperatorBusy(String group) throws AceServerException {
		try {
			String val = PolledAppServerAdapter.getInstance().getParam(
					"com.quikj.application.web.talk.feature.operator.Operator:"
							+ group, "all-operators-busy");

			boolean busy;
			if (val == null) {
				busy = true;
			} else if (val.equals("false")) {
				busy = false;
			} else {
				busy = true;
			}

			if (busy) {
				OpBusyCDR cdr = new OpBusyCDR(group);
				PolledAppServerAdapter.getInstance().writeCdr(cdr);
			}

			return busy;
		} catch (AppServerAdapterException e) {
			e.printStackTrace();
			throw formatServerException(e);
		}
	}

	@Override
	public GroupInfo[] getGroupInfo(String user) {
		return PolledAppServerAdapter.getInstance().getGroupInfo(user);
	}

	@Override
	public String sendMail(SendMailRequestMessage mail, String captcha,
			String captchaType) {
		String salt = getThreadLocalRequest().getSession().getId();
		try {
			boolean correct = CaptchaService.getInstance().verify(salt, captcha);
			if (!correct) {
				return "NoMatch";
			}
		} catch (Exception e) {
			// should not happen, may be thrown if the id is not valid
			e.printStackTrace();
			return "NoMatch";
		}

		MailElement rcvd = mail.getMailElement();
		AceMailMessage send = new AceMailMessage();

		send.setSubType(rcvd.getSubype());

		int num_items = rcvd.numBcc();
		for (int i = 0; i < num_items; i++) {
			send.addBcc(rcvd.getBccAt(i));
		}

		num_items = rcvd.numCc();
		for (int i = 0; i < num_items; i++) {
			send.addCc(rcvd.getCcAt(i));
		}

		num_items = rcvd.numTo();
		for (int i = 0; i < num_items; i++) {
			send.addTo(rcvd.getToAt(i));
		}

		num_items = rcvd.numReplyTo();
		for (int i = 0; i < num_items; i++) {
			send.addReplyTo(rcvd.getReplyToAt(i));
		}

		String rcv_body = rcvd.getBody();
		if (rcv_body != null) {
			send.setBody(rcv_body);
		}

		String rcv_from = rcvd.getFrom();
		if (rcv_from != null) {
			send.setFrom(rcv_from);
		}

		String rcv_subject = rcvd.getSubject();
		if (rcv_subject != null) {
			send.setSubject(rcv_subject);
		}

		AceMailService.getInstance().addToMailQueue(send);
		return null;
	}

	private AceServerException formatServerException(AppServerAdapterException e) {
		// Do not change the [Internal Server Error] since the client uses this
		// part of the message to determine if it is a Ace Operator exception or
		// some other exception

		return new AceServerException(e.getMessage(), e.isRecoverable());
	}

	@Override
	public HashMap<Integer, String> getSecurityQuestions(String userid,
			String captcha, String captchaType) throws AceServerException {
		validateCaptcha(captcha, captchaType);

		try {
			return PolledAppServerAdapter.getInstance().getSecurityQuestions(
					userid);
		} catch (AppServerAdapterException e) {
			e.printStackTrace();
			throw formatServerException(e);
		}
	}

	@Override
	public void resetPassword(String userid, String captcha,
			String captchaType, HashMap<Integer, String> securityAnswers,
			String locale) throws AceServerException {
		validateCaptcha(captcha, captchaType);

		try {
			PolledAppServerAdapter.getInstance().resetPassword(userid,
					securityAnswers, locale);
		} catch (AppServerAdapterException e) {
			e.printStackTrace();
			throw formatServerException(e);
		}
	}

	private void validateCaptcha(String captcha, String captchaType)
			throws AceServerException {
		String salt = getThreadLocalRequest().getSession().getId();
		try {
			boolean correct = CaptchaService.getInstance().verify(salt, captcha);
			if (!correct) {
				throw new AceServerException("unmatchedCapchaChars", true);
			}
		} catch (Exception e) {
			// should not happen, may be thrown if the id is not valid
			e.printStackTrace();
			throw new AceServerException("imagePasscodeErrorTryAgain", true);
		}
	}

	@Override
	public void recoverLostUsername(String address, String captcha,
			String captchaType, String locale) throws AceServerException {
		validateCaptcha(captcha, captchaType);

		try {
			PolledAppServerAdapter.getInstance().recoverLostUsername(address,
					locale);
		} catch (AppServerAdapterException e) {
			e.printStackTrace();
			throw formatServerException(e);
		}
	}
}
