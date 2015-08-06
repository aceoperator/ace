package com.quikj.ace.web.client;

import java.util.HashMap;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.quikj.ace.messages.vo.adapter.GroupInfo;
import com.quikj.ace.messages.vo.app.Message;
import com.quikj.ace.messages.vo.talk.CannedMessageElement;
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

	public List<Message> exchangeMessages(Message incoming)
			throws AceServerException;

	public List<Message> exchangeMessages(List<Message> incoming)
			throws AceServerException;

	public String connect() throws AceServerException;

	public void disconnect(String sessionId) throws AceServerException;

	public CannedMessageElement[] listCannedMessages(String[] groups, boolean fetchContent)
			throws AceServerException;

	public HashMap<String, String> getProfile(String groupName,
			String browserType) throws AceServerException;

	public boolean allOperatorBusy(String group) throws AceServerException;

	public String sendMail(SendMailRequestMessage mail, String captcha,
			String captchaType);

	public GroupInfo[] getGroupInfo(String user);

	public HashMap<Integer, String> getSecurityQuestions(String userid,
			String captcha, String captchaType) throws AceServerException;

	public void resetPassword(String userid, String captcha,
			String captchaType, HashMap<Integer, String> securityAnswers,
			String locale) throws AceServerException;

	public void recoverLostUsername(String address, String captcha,
			String captchaType, String locale) throws AceServerException;
}
