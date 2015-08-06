/**
 * 
 */
package com.quikj.ace.web.client;

import java.util.HashMap;
import java.util.List;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.quikj.ace.messages.vo.adapter.GroupInfo;
import com.quikj.ace.messages.vo.app.Message;
import com.quikj.ace.messages.vo.talk.CannedMessageElement;
import com.quikj.ace.messages.vo.talk.SendMailRequestMessage;

/**
 * @author amit
 * 
 */
public interface AceOperatorServiceAsync {

	public RequestBuilder exchangeMessages(Message incoming,
			AsyncCallback<List<Message>> callback);

	public RequestBuilder exchangeMessages(List<Message> incoming,
			AsyncCallback<List<Message>> callback);

	public RequestBuilder connect(AsyncCallback<String> callback);

	public RequestBuilder disconnect(String sessionId,
			AsyncCallback<Void> callback);

	public RequestBuilder listCannedMessages(String[] groups,
			boolean fetchContent,
			AsyncCallback<CannedMessageElement[]> callback);

	public RequestBuilder getProfile(String profileName, String browserType,
			AsyncCallback<HashMap<String, String>> callback);

	public RequestBuilder allOperatorBusy(String group,
			AsyncCallback<Boolean> callback);

	public RequestBuilder getGroupInfo(String user,
			AsyncCallback<GroupInfo[]> callback);

	public RequestBuilder sendMail(SendMailRequestMessage mail, String captcha,
			String captchaType, AsyncCallback<String> callback);

	public RequestBuilder getSecurityQuestions(String userid, String captcha,
			String captchaType, AsyncCallback<HashMap<Integer, String>> callback);

	public RequestBuilder resetPassword(String userid, String captcha,
			String captchaType, HashMap<Integer, String> securityAnswers,
			String locale, AsyncCallback<Void> callback);

	public RequestBuilder recoverLostUsername(String address, String captcha,
			String captchaType, String locale, AsyncCallback<Void> asyncCallback);
}
