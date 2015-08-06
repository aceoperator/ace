/**
 * 
 */
package com.quikj.application.web.talk.plugin;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import com.quikj.server.framework.AceException;
import com.quikj.server.framework.AceLogger;
import com.quikj.server.framework.AceMailMessage;
import com.quikj.server.framework.AceMailService;

/**
 * @author becky
 * 
 */
public class LostUsernamePassword {

	private static final String RANDOM_PASSWORD_CHARS = "abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ23456789+@";
	private static final int PASSWORD_LENGTH = 12;

	public static void resetPassword(String userid,
			HashMap<Integer, String> securityAnswers, String locale)
			throws AceException {

		if (userid == null || securityAnswers == null
				|| securityAnswers.isEmpty()) {
			throw new AceException(
					"The userid or security answers have not been supplied",
					true);
		}

		// generate the password
		StringBuffer password = new StringBuffer();
		for (int i = 0; i < PASSWORD_LENGTH; i++) {
			int index = (int) (Math.random() * RANDOM_PASSWORD_CHARS.length());
			password.append(RANDOM_PASSWORD_CHARS.charAt(index));
		}

		// change the password in the database
		boolean passwordChanged = false;
		try {
			passwordChanged = SynchronousDbOperations
					.getInstance()
					.resetPassword(userid, password.toString(), securityAnswers);
		} catch (Exception e) {
			throw new AceException("databaseError", true);
		}

		if (!passwordChanged) {
			throw new AceException("unmatchedAnswers", true);
		}

		// email the password
		try {
			String emailAddress = SynchronousDbOperations.getInstance()
					.getEmailAddress(userid);
			if (emailAddress == null || emailAddress.trim().length() == 0) {
				// shouldn't happen, checked when security questions obtained
				throw new Exception("No email address in database");
			}

			AceMailMessage msg = new AceMailMessage();
			msg.addTo(emailAddress);

			ResourceBundle messages = ResourceBundle.getBundle(
					"com.quikj.application.web.talk.plugin.language",
					ServiceController.getLocale(locale));

			String body = MessageFormat.format(
					messages.getString("Lost_password_email_body"), userid,
					password.toString());

			msg.setBody(body);
			msg.setSubject(messages.getString("Lost_password_email_subject"));

			if (AceMailService.getInstance().addToMailQueue(msg) == false) {
				throw new Exception(
						"Adding the outbound mail to the mail queue failed");
			}
		} catch (Exception e) {
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							"SelfServiceOperations.resetPassword() -- Password for user '"
									+ userid
									+ "' has been reset, but the user email could not be sent : "
									+ e.getMessage());
			throw new AceException("resetPasswordEmailFailed", false);
		}
	}

	public static void recoverLostUsername(String address, String locale)
			throws AceException {

		if (address == null) {
			throw new AceException(
					"The email address attribute has not been supplied", true);
		}

		List<String> users = null;
		try {
			users = SynchronousDbOperations.getInstance()
					.findUserByEmailAddress(address);
		} catch (Exception e) {
			throw new AceException("databaseError", true);
		}

		if (users == null || users.isEmpty()) {
			throw new AceException("userNotFound", true);
		}

		// email the user name
		try {
			AceMailMessage msg = new AceMailMessage();
			msg.addTo(address);

			ResourceBundle messages = ResourceBundle.getBundle(
					"com.quikj.application.web.talk.plugin.language",
					ServiceController.getLocale(locale));

			String body = null;
			if (users.size() == 1) {
				body = MessageFormat.format(
						messages.getString("Lost_username_email_body"),
						users.get(0));
			} else {
				StringBuilder buf = new StringBuilder();
				for (int i = 0; i < users.size(); i++) {
					if (i > 0) {
						buf.append(", ");
					}

					buf.append(users.get(i));
				}

				body = MessageFormat.format(
						messages.getString("Lost_usernames_email_body"),
						buf.toString());
			}

			msg.setBody(body);
			msg.setSubject(messages.getString("Lost_username_email_subject"));

			if (AceMailService.getInstance().addToMailQueue(msg) == false) {
				throw new Exception(
						"Adding the outbound mail to the mail queue failed");
			}
		} catch (Exception e) {
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							"SelfServiceOperations.recoverLostUsername() -- User '"
									+ users.get(0)
									+ "' for email address '"
									+ address
									+ "' has been recovered, but the user email could not be sent : "
									+ e.getMessage());
			throw new AceException("usernameEmailFailed", false);
		}
	}
}
