/*
 * UserManagementAction.java
 *
 */

package com.quikj.application.communicator.applications.webtalk.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.LabelValueBean;

import com.quikj.ace.db.core.webtalk.vo.SecurityQuestion;
import com.quikj.ace.db.core.webtalk.vo.User;
import com.quikj.ace.db.webtalk.model.GroupBean;
import com.quikj.ace.db.webtalk.model.UserBean;
import com.quikj.application.communicator.admin.controller.LinkAttribute;
import com.quikj.application.communicator.admin.controller.SpringUtils;
import com.quikj.client.raccess.RemoteAccessClient;
import com.quikj.server.framework.AceLogger;

/**
 * 
 * @author bhm
 */
public class UserManagementAction extends Action {

	public UserManagementAction() {
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws UnsupportedEncodingException {
		UserManagementForm uform = (UserManagementForm) form;

		ActionErrors errors = new ActionErrors();

		GroupBean groupBean = SpringUtils.getBean(request.getSession()
				.getServletContext(), GroupBean.class);
		ArrayList<LabelValueBean> list = GroupManagementAction.populateGroups(
				groupBean, null);
		uform.setUserGroups(list);
		
		if (uform.getSubmit().equals("Find")) {
			try {
				UserBean userBean = SpringUtils.getBean(request.getSession()
						.getServletContext(), UserBean.class);
				User user = userBean.getUserByName(uform.getName());
				uform.setAdditionalInfo(user.getAdditionalInfo());
				uform.setAddress(user.getEmail());
				uform.setFullName(user.getFullName());
				uform.setName(user.getUserName());
				uform.setUnavailXferTo(user.getUnavailableTransferTo());
				uform.setGatekeeper(user.getGatekeeper());
				uform.setAvatar(user.getAvatar());
				uform.setLocked(user.isLocked());
				uform.setChangePassword(user.isChangePassword());
				uform.setDomain(user.getDomain());
				uform.setPrivateInfo(user.isPrivateInfo());

				uform.setFlags(null);
				if (user.getFlags() > 0) {
					uform.setFlags("#" + Integer.toHexString(user.getFlags()));
				}

				List<String> ownsGroup = user.getOwnsGroups();
				Object[] owns = new Object[ownsGroup.size()];
				uform.setOwnsGroups(owns);
				for (int i = 0; i < owns.length; i++) {
					owns[i] = URLEncoder.encode(ownsGroup.get(i), "UTF-8");
				}

				List<String> belongsToGroup = user.getMemberOfGroups();
				Object[] belongs = new Object[belongsToGroup.size()];
				uform.setBelongsToGroups(belongs);
				for (int i = 0; i < belongs.length; i++) {
					belongs[i] = URLEncoder.encode(belongsToGroup.get(i),
							"UTF-8");
				}

				uform.clearSecurityQuestions();
				for (SecurityQuestion q : user.getSecurityQuestions()) {
					switch (q.getQuestionId()) {
					case 0:
						uform.setSecurityQuestion1(q.getQuestion());
						uform.setSecurityAnswer1(q.getAnswer());
						break;
					case 1:
						uform.setSecurityQuestion2(q.getQuestion());
						uform.setSecurityAnswer2(q.getAnswer());
						break;
					case 2:
						uform.setSecurityQuestion3(q.getQuestion());
						uform.setSecurityAnswer3(q.getAnswer());
						break;
					default:
						AceLogger
								.Instance()
								.log(AceLogger.ERROR,
										AceLogger.SYSTEM_LOG,
										"UserManagementAction.execute()/Find/by-"
												+ uform.getName()
												+ ": Unsupported security question ID retrieved: "
												+ q.getId());
						break;
					}
				}
			} catch (Exception e) {
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
						"error.no.such.user"));
			}

		} else if (uform.getSubmit().equals("Modify")) {
			if (!loggedIn(request, uform.getName(), errors, "Modify/by-"
					+ request.getUserPrincipal().getName())) {
				User user = new User();
				user.setAdditionalInfo(uform.getAdditionalInfo());
				user.setEmail(uform.getAddress());
				user.setFullName(uform.getFullName());
				user.setUserName(uform.getName());
				user.setUnavailableTransferTo(uform.getUnavailXferTo());
				user.setGatekeeper(uform.getGatekeeper());
				user.setLocked(uform.isLocked());
				user.setChangePassword(uform.isChangePassword());
				user.setDomain(uform.getDomain());
				user.setPrivateInfo(uform.isPrivateInfo());

				if (uform.getAvatar() != null && uform.getAvatar().length() > 0) {
					user.setAvatar(uform.getAvatar());
				} else {
					user.setAvatar(null);
				}

				if (uform.getFlags() != null && uform.getFlags().length() > 0) {
					user.setFlags(Integer.decode(uform.getFlags()));
				}

				String password = uform.getPassword();
				if ((password != null) && (password.length() > 0)) {
					user.setPassword(password);
				}

				Object[] ogroups = uform.getOwnsGroups();
				if (ogroups != null) {
					for (int i = 0; i < ogroups.length; i++) {
						if (ogroups[i] != null) {
							String decodedGroup = URLDecoder.decode(
									(String) ogroups[i], "UTF-8");
							user.getOwnsGroups().add(decodedGroup);
						}
					}
				}

				ogroups = uform.getBelongsToGroups();
				if (ogroups != null) {
					for (int i = 0; i < ogroups.length; i++) {
						if (ogroups[i] != null) {
							String decodedGroups = URLDecoder.decode(
									(String) ogroups[i], "UTF-8");

							user.getMemberOfGroups().add(decodedGroups);
						}
					}
				}

				String question = uform.getSecurityQuestion1();
				if (question != null && question.length() > 0) {
					SecurityQuestion q = new SecurityQuestion();
					q.setQuestionId(0);
					q.setQuestion(question);
					String answer = uform.getSecurityAnswer1();
					if (answer != null && answer.length() > 0) {
						q.setAnswer(answer);
					}
					user.getSecurityQuestions().add(q);
				}

				question = uform.getSecurityQuestion2();
				if (question != null && question.length() > 0) {
					SecurityQuestion q = new SecurityQuestion();
					q.setQuestionId(1);
					q.setQuestion(question);
					String answer = uform.getSecurityAnswer2();
					if (answer != null && answer.length() > 0) {
						q.setAnswer(answer);
					}
					user.getSecurityQuestions().add(q);
				}

				question = uform.getSecurityQuestion3();
				if (question != null && question.length() > 0) {
					SecurityQuestion q = new SecurityQuestion();
					q.setQuestionId(2);
					q.setQuestion(question);
					String answer = uform.getSecurityAnswer3();
					if (answer != null && answer.length() > 0) {
						q.setAnswer(answer);
					}
					user.getSecurityQuestions().add(q);
				}

				try {
					UserBean userBean = SpringUtils.getBean(request
							.getSession().getServletContext(), UserBean.class);
					userBean.modifyUser(user);

					AceLogger.Instance().log(
							AceLogger.INFORMATIONAL,
							AceLogger.USER_LOG,
							"User " + request.getUserPrincipal().getName()
									+ " modified webtalk user "
									+ uform.getName());

					// forward control to the webtalk main menu
					ActionMessages messages = new ActionMessages();
					messages.add(ActionMessages.GLOBAL_MESSAGE,
							new ActionMessage("message.user.modified"));

					saveMessages(request, messages);
					return mapping.findForward("webtalk_main_menu");
				} catch (Exception e) {
					errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
							"error.db.failure"));

					AceLogger.Instance()
							.log(AceLogger.ERROR,
									AceLogger.SYSTEM_LOG,
									"UserManagementAction.execute()/Modify/by-"
											+ request.getUserPrincipal().getName() + ": "
											+ e.getMessage());
				}
			}
		} else if (uform.getSubmit().equals("Create")) {
			User user = new User();

			user.setAdditionalInfo(uform.getAdditionalInfo());
			user.setEmail(uform.getAddress());
			user.setFullName(uform.getFullName());
			user.setUserName(uform.getName());
			user.setUnavailableTransferTo(uform.getUnavailXferTo());
			user.setGatekeeper(uform.getGatekeeper());
			user.setPassword(uform.getPassword());
			user.setLocked(uform.isLocked());
			user.setChangePassword(uform.isChangePassword());
			user.setDomain(uform.getDomain());
			user.setPrivateInfo(uform.isPrivateInfo());

			if (uform.getFlags() != null && uform.getFlags().length() > 0) {
				user.setFlags(Integer.decode(uform.getFlags()));
			}

			if (uform.getAvatar() != null && uform.getAvatar().length() > 0) {
				user.setAvatar(uform.getAvatar());
			} else {
				user.setAvatar(null);
			}

			String answer = uform.getSecurityAnswer1();
			if (answer != null && answer.length() > 0) {
				SecurityQuestion q = new SecurityQuestion();
				q.setQuestionId(0);
				q.setQuestion(uform.getSecurityQuestion1());
				q.setAnswer(answer);
				user.getSecurityQuestions().add(q);
			}

			answer = uform.getSecurityAnswer2();
			if (answer != null && answer.length() > 0) {
				SecurityQuestion q = new SecurityQuestion();
				q.setQuestionId(1);
				q.setQuestion(uform.getSecurityQuestion2());
				q.setAnswer(answer);
				user.getSecurityQuestions().add(q);
			}

			answer = uform.getSecurityAnswer3();
			if (answer != null && answer.length() > 0) {
				SecurityQuestion q = new SecurityQuestion();
				q.setQuestionId(2);
				q.setQuestion(uform.getSecurityQuestion3());
				q.setAnswer(answer);
				user.getSecurityQuestions().add(q);
			}

			Object[] ogroups = uform.getOwnsGroups();
			if (ogroups != null) {
				for (int i = 0; i < ogroups.length; i++) {
					if (ogroups[i] != null) {
						String decodedGroup = URLDecoder.decode(
								(String) ogroups[i], "UTF-8");
						user.getOwnsGroups().add(decodedGroup);
					}
				}
			}

			ogroups = uform.getBelongsToGroups();
			if (ogroups != null) {
				for (int i = 0; i < ogroups.length; i++) {
					if (ogroups[i] != null) {
						String decodedGroup = URLDecoder.decode(
								(String) ogroups[i], "UTF-8");
						user.getMemberOfGroups().add(decodedGroup);
					}
				}
			}

			try {
				UserBean userBean = SpringUtils.getBean(request.getSession()
						.getServletContext(), UserBean.class);
				userBean.createUser(user);

				AceLogger.Instance().log(
						AceLogger.INFORMATIONAL,
						AceLogger.USER_LOG,
						"User " + request.getUserPrincipal().getName() + " created webtalk user "
								+ uform.getName());

				// forward control to the webtalk main menu
				ActionMessages messages = new ActionMessages();
				messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
						"message.user.created"));
				saveMessages(request, messages);
				return mapping.findForward("webtalk_main_menu");
			} catch (Exception e) {
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
						"error.account.create.failure"));

				AceLogger.Instance().log(
						AceLogger.ERROR,
						AceLogger.SYSTEM_LOG,
						"UserManagementAction.execute()/Create/by-"
								+ request.getUserPrincipal().getName() + ": " + e.getMessage());
			}
		} else if (uform.getSubmit().equals("Delete")) {
			if (!loggedIn(request, uform.getName(), errors, "Delete/by-"
					+ request.getUserPrincipal().getName())) {

				try {
					UserBean userBean = SpringUtils.getBean(request
							.getSession().getServletContext(), UserBean.class);
					userBean.removeUser(uform.getName());

					AceLogger.Instance().log(
							AceLogger.INFORMATIONAL,
							AceLogger.USER_LOG,
							"User " + request.getUserPrincipal().getName()
									+ " deleted webtalk user "
									+ uform.getName());

					// forward control to the webtalk main menu
					ActionMessages messages = new ActionMessages();
					messages.add(ActionMessages.GLOBAL_MESSAGE,
							new ActionMessage("message.user.deleted"));
					saveMessages(request, messages);
					return mapping.findForward("webtalk_main_menu");
				} catch (Exception e) {
					errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
							"error.db.failure"));

					AceLogger.Instance()
							.log(AceLogger.ERROR,
									AceLogger.SYSTEM_LOG,
									"UserManagementAction.execute()/Delete/by-"
											+ request.getUserPrincipal().getName() + ": "
											+ e.getMessage());
				}
			}
		} else if (uform.getSubmit().equals("Manage Blacklist")) {
			UserBean userBean = SpringUtils.getBean(request.getSession()
					.getServletContext(), UserBean.class);

			try {
				userBean.getUserId(uform.getName());
				request.setAttribute("userContext", uform.getName());
				return mapping.findForward("list_blacklist");
			} catch (Exception e) {
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
						"error.no.such.user"));
			}
		}

		if (!errors.isEmpty()) {
			saveErrors(request, errors);
		}

		// add related tasks to the navigation bar
		WebTalkRelatedTasks menu = new WebTalkRelatedTasks();
		menu.addLink(new LinkAttribute("Search users", "display_user_search"));
		menu.addLink(new LinkAttribute("List all groups", "list_groups"));
		menu.addLink(new LinkAttribute("Administer groups",
				"display_group_management"));
		request.setAttribute("menu", menu);

		return mapping.getInputForward();
	}

	private boolean loggedIn(HttpServletRequest request, String user,
			ActionErrors errors, String log_prefix) {
		RemoteAccessClient cl = (RemoteAccessClient) request.getSession()
				.getServletContext().getAttribute("remoteAccess");
		if (cl == null) {
			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
					"error.rmi.error"));

			AceLogger.Instance().log(
					AceLogger.ERROR,
					AceLogger.SYSTEM_LOG,
					"UserManagementAction.loggedIn()/" + log_prefix
							+ ": Could not obtain RMI client object");

			return true;
		}

		try {
			String val = cl.getRemoteAccess().getParam(
					"com.quikj.application.web.talk.plugin.ServiceController",
					"logged-in:" + user);
			if (val == null) {
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
						"error.rmi.error"));

				AceLogger
						.Instance()
						.log(AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								"UserManagementAction.loggedIn()/"
										+ log_prefix
										+ ": Could not obtain logged-in param from ServiceController");

				return true;
			} else if (val.equals("no") == false) {
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
						"error.rmi.logged.in"));
				return true;
			}
		} catch (Exception ex) {
			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
					"error.rmi.error"));

			AceLogger.Instance().log(
					AceLogger.ERROR,
					AceLogger.SYSTEM_LOG,
					"UserManagementAction.loggedIn()/" + log_prefix
							+ ex.getClass().getName() + ": " + ex.getMessage());

			return true;
		}
		return false;
	}
}
