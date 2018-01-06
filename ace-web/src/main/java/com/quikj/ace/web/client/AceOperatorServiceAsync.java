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
import com.quikj.ace.messages.vo.talk.FormSubmissionElement;
import com.quikj.ace.messages.vo.talk.SendMailRequestMessage;

/**
 * @author amit
 * 
 */
public interface AceOperatorServiceAsync {

	RequestBuilder exchangeMessages(Message incoming,
			AsyncCallback<List<Message>> callback);

	RequestBuilder exchangeMessages(List<Message> incoming,
			AsyncCallback<List<Message>> callback);

	RequestBuilder connect(AsyncCallback<String> callback);

	RequestBuilder disconnect(String sessionId,
			AsyncCallback<Void> callback);

	RequestBuilder listCannedMessages(String[] groups,
			boolean fetchContent,
			AsyncCallback<CannedMessageElement[]> callback);

	RequestBuilder getProfile(String profileName, String browserType,
			AsyncCallback<HashMap<String, String>> callback);

	RequestBuilder allOperatorBusy(String group,
			AsyncCallback<Boolean> callback);

	RequestBuilder getGroupInfo(String user,
			AsyncCallback<GroupInfo[]> callback);

	RequestBuilder sendMail(SendMailRequestMessage mail, String captcha,
			String captchaType, AsyncCallback<String> callback);

	RequestBuilder getSecurityQuestions(String userid, String captcha,
			String captchaType, AsyncCallback<HashMap<Integer, String>> callback);

	RequestBuilder resetPassword(String userid, String captcha,
			String captchaType, HashMap<Integer, String> securityAnswers,
			String locale, AsyncCallback<Void> callback);

	RequestBuilder recoverLostUsername(String address, String captcha,
			String captchaType, String locale, AsyncCallback<Void> asyncCallback);

	RequestBuilder submitForm(FormSubmissionElement form, AsyncCallback<Void> callback);
}