/*
 * GroupWizardCustomerInfoAction.java
 *
 */

package com.quikj.application.communicator.applications.webtalk.controller;

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
import org.apache.struts.action.DynaActionForm;

import com.quikj.ace.db.core.webtalk.vo.Group;
import com.quikj.ace.db.core.webtalk.vo.User;
import com.quikj.ace.db.webtalk.WebTalkException;
import com.quikj.ace.db.webtalk.model.GroupBean;
import com.quikj.ace.db.webtalk.model.UserBean;
import com.quikj.application.communicator.admin.controller.LinkAttribute;
import com.quikj.application.communicator.admin.controller.SpringUtils;
import com.quikj.server.framework.AceLogger;

/**
 * 
 * @author bhm
 */
public class GroupWizardCustomerInfoAction extends Action {

	public GroupWizardCustomerInfoAction() {
	}

	private boolean commonGroupNameAvailable(String name, GroupBean groupBean) {

		// check for groups named name
		try {
			groupBean.findByName(name);
			return false;
		} catch (WebTalkException e) {
			// group not found, move on
		} catch (Exception e) {
			AceLogger.Instance().log(
					AceLogger.ERROR,
					AceLogger.SYSTEM_LOG,
					"GroupWizardCustomerInfoAction.commonGroupNameAvailable(): "
							+ e.getMessage());

			return false;
		}

		// check for groups with domain = name
		try {
			List<Group> list = groupBean.listGroups(name);
			if (list.size() > 0) {
				return false;
			}
		} catch (Exception e) {
			AceLogger.Instance().log(
					AceLogger.ERROR,
					AceLogger.SYSTEM_LOG,
					"GroupWizardCustomerInfoAction.commonGroupNameAvailable(): "
							+ e.getMessage());

			return false;
		}

		// check for webtalk user called name-owner
		try {
			UserBean userBean = SpringUtils.getBean(getServlet()
					.getServletContext(), UserBean.class);
			userBean.getUserByName(name + "-owner");
			return false;
		} catch (Exception e) {
			return true;
		}
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		ActionErrors errors = new ActionErrors();

		String submit = (String) ((DynaActionForm) form).get("submit");

		if (submit.equals("Next")) {
			String nickname = ((String) ((DynaActionForm) form)
					.get("companyNickname")).trim();

			GroupBean groupBean = SpringUtils.getBean(request.getSession()
					.getServletContext(), GroupBean.class);

			if (commonGroupNameAvailable(nickname, groupBean)) {
				Group ele = new Group();

				ele.setMemberBusyNotification(GroupBean.NOTIFY_ALL);
				ele.setMemberLoginNotification(GroupBean.NOTIFY_ALL);
				ele.setOwnerBusyNotification(GroupBean.NOTIFY_NONE);
				ele.setOwnerLoginNotification(GroupBean.NOTIFY_NONE);

				ele.setName(nickname);
				ele.setDomain(nickname);

				try {
					groupBean.createGroup(ele);
					

					AceLogger.Instance().log(
							AceLogger.INFORMATIONAL,
							AceLogger.USER_LOG,
							"User " + request.getUserPrincipal().getName() + " created new group "
									+ nickname);

					String company_name = ((String) ((DynaActionForm) form)
							.get("companyName")).trim();
					String company_url = ((String) ((DynaActionForm) form)
							.get("companyUrl")).trim();

					// create wizard action log
					ArrayList log = new ArrayList();

					StringBuffer buf = new StringBuffer(
							"Informational - Customer name:    ");
					buf.append(company_name);
					buf.append("\n");
					log.add(buf.toString());

					buf = new StringBuffer("Informational - Customer acronym: ");
					buf.append(nickname);
					buf.append("\n");
					log.add(buf.toString());

					buf = new StringBuffer("Informational - Customer webpage: ");
					buf.append(company_url);
					buf.append("\n");
					log.add(buf.toString());

					log.add("\n");

					buf = new StringBuffer("Created common customer group '");
					buf.append(nickname);
					buf.append("'\n");
					log.add(buf.toString());

					// set session scope wizard vars
					request.getSession().setAttribute("groupWizardLog", log);
					request.getSession().setAttribute("groupWizardDomain",
							nickname);
					request.getSession().setAttribute("groupWizardCompanyName",
							company_name);
					request.getSession().setAttribute("groupWizardCompanyUrl",
							company_url);

					// now add the group owner, every group with operators
					// belonging to it must have one

					String groupowner_name = nickname + "-owner";

					User u = new User();
					u.setAdditionalInfo("Owner of group " + nickname
							+ ", company: " + company_name + ", url: "
							+ company_url);
					u.setFullName(nickname + " Group Owner");
					u.setUserName(groupowner_name);
					u.setPassword("a1b2c3d4");

					u.getOwnsGroups().add(nickname);

					try {
						UserBean userBean = SpringUtils.getBean(request
								.getSession().getServletContext(),
								UserBean.class);
						userBean.createUser(u);

						AceLogger.Instance().log(
								AceLogger.INFORMATIONAL,
								AceLogger.USER_LOG,
								"User " + request.getUserPrincipal().getName()
										+ " created new webtalk user "
										+ groupowner_name);

						// append to wizard action log
						buf = new StringBuffer("\nCreated common group owner '");
						buf.append(groupowner_name);
						buf.append("'\n");
						log.add(buf.toString());

					} catch (Exception e) {
						errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
								"error.common.groupowner.create.failure",
								new String(groupowner_name), new String(
										nickname)));

						AceLogger.Instance().log(
								AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								"GroupWizardCustomerInfoAction.execute()/Create Group Owner/by-"
										+ request.getUserPrincipal().getName() + ": "
										+ e.getMessage(), e);

						// append to wizard action log
						buf = new StringBuffer(
								"\n***  ERROR  *** creating group owner : The group owner named '");
						buf.append(groupowner_name);
						buf.append("' for the new group '");
						buf.append(nickname);
						buf.append("' could not be created because a user with that name already exists or there was a database error. If that user name already exists in the system, proceed with the wizard as you normally would. After finishing with the wizard, add the group owner for this new group yourself, using a unique name other than '");
						buf.append(groupowner_name);
						buf.append("'. Assign its domain = ");
						buf.append(nickname);
						buf.append(", give it any password, and be sure to mark it as owning group '");
						buf.append(nickname);
						buf.append("'.");
						buf.append("\n");
						log.add(buf.toString());
					}				
				} catch (Exception e) {
					errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
							"error.common.group.create.failure", nickname));

					AceLogger.Instance().log(
							AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							"GroupWizardCustomerInfoAction.execute()/Create Common Group/by-"
									+ request.getUserPrincipal().getName() + ": "
									+ e.getMessage());
				}
			} else {
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
						"error.company.acronym.unavailable"));
			}
		}

		if (!errors.isEmpty()) {
			saveErrors(request, errors);
			return mapping.getInputForward();
		}

		if (!submit.startsWith("Cancel")) {
			// add related tasks to the navigation bar
			WebTalkRelatedTasks menu = new WebTalkRelatedTasks();
			menu.addLink(new LinkAttribute("List all groups", "list_groups"));
			menu.addLink(new LinkAttribute("List all features", "list_features"));
			menu.addLink(new LinkAttribute("Search users",
					"display_user_search"));

			request.setAttribute("menu", menu);
		}

		return mapping.findForward(submit);
	}
}
