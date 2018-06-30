package com.quikj.ace.web.client;

import java.util.HashMap;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.quikj.ace.messages.vo.adapter.GroupInfo;
import com.quikj.ace.messages.vo.app.Message;
import com.quikj.ace.messages.vo.talk.CannedMessageElement;
import com.quikj.ace.messages.vo.talk.FormSubmissionElement;
import com.quikj.ace.messages.vo.talk.SendMailRequestMessage;
import com.quikj.ace.web.shared.AceServerException;

@RemoteServiceRelativePath("AceOperatorService")
public interface AceOperatorService extends RemoteService {

	public static class Util {
		private static AceOperatorServiceAsync instance;

		public static AceOperatorServiceAsync getInstance() {
			if (instance == null) {
				instance = GWT.create(AceOperatorService.class);
			}
			return instance;
		}
	}

	public static String ACE_ENDUSER_COOKIE_NAME = "com.quikj.ace.endUserIdentifier";

	List<Message> exchangeMessages(Message incoming) throws AceServerException;

	List<Message> exchangeMessages(List<Message> incoming) throws AceServerException;

	String connect() throws AceServerException;

	void disconnect(String sessionId) throws AceServerException;

	CannedMessageElement[] listCannedMessages(String[] groups, boolean fetchContent) throws AceServerException;

	HashMap<String, String> getProfile(String groupName, String browserType) throws AceServerException;

	boolean allOperatorBusy(String group) throws AceServerException;

	String sendMail(SendMailRequestMessage mail, String captcha, String captchaType);

	GroupInfo[] getGroupInfo(String user);

	HashMap<Integer, String> getSecurityQuestions(String userid, String captcha, String captchaType)
			throws AceServerException;

	void resetPassword(String userid, String captcha, String captchaType, HashMap<Integer, String> securityAnswers,
			String locale) throws AceServerException;

	void recoverLostUsername(String address, String captcha, String captchaType, String locale)
			throws AceServerException;
	
	void submitForm(FormSubmissionElement form) throws AceServerException;
}
