/*
 * GroupWizardGroupAddAction.java
 *
 */

package com.quikj.application.communicator.applications.webtalk.controller;

import java.util.ArrayList;

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
import org.apache.struts.action.DynaActionForm;

import com.quikj.ace.db.core.webtalk.vo.Feature;
import com.quikj.ace.db.core.webtalk.vo.FeatureParam;
import com.quikj.ace.db.core.webtalk.vo.Group;
import com.quikj.ace.db.core.webtalk.vo.User;
import com.quikj.ace.db.webtalk.model.FeatureBean;
import com.quikj.ace.db.webtalk.model.GroupBean;
import com.quikj.ace.db.webtalk.model.UserBean;
import com.quikj.application.communicator.admin.controller.LinkAttribute;
import com.quikj.application.communicator.admin.controller.SpringUtils;
import com.quikj.server.framework.AceLogger;

/**
 * 
 * @author bhm
 */
public class GroupWizardGroupAddAction extends Action {

	public GroupWizardGroupAddAction() {
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		ActionErrors errors = new ActionErrors();

		ArrayList log = (ArrayList) request.getSession().getAttribute(
				"groupWizardLog");
		if (log == null) {
			// did not get here through normal sequence

			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
					"error.hosting.wizard.sequence"));
			saveErrors(request, errors);

			return mapping.findForward("display_group_wizard_intro");
		}

		String submit = (String) ((DynaActionForm) form).get("submit");

		if (submit.equals("Add Group")) {
			String domain = (String) request.getSession().getAttribute(
					"groupWizardDomain");
			String group_name = domain
					+ '-'
					+ ((String) ((DynaActionForm) form).get("groupName"))
							.trim();

			GroupBean groupBean = SpringUtils.getBean(request.getSession()
					.getServletContext(), GroupBean.class);

			Group ele = new Group();

			ele.setMemberBusyNotification(GroupBean.NOTIFY_ALL);
			ele.setMemberLoginNotification(GroupBean.NOTIFY_ALL);
			ele.setOwnerBusyNotification(GroupBean.NOTIFY_NONE);
			ele.setOwnerLoginNotification(GroupBean.NOTIFY_NONE);
			ele.setName(group_name);
			ele.setDomain(domain);

			try {
				groupBean.createGroup(ele);

				AceLogger.Instance().log(
						AceLogger.INFORMATIONAL,
						AceLogger.USER_LOG,
						"User " + request.getUserPrincipal().getName() + " created new group "
								+ group_name);

				ActionMessages messages = new ActionMessages();
				messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
						"message.group.x.created", new String(group_name)));
				saveMessages(request, messages);

				// append to wizard action log
				StringBuffer buf = new StringBuffer("\nCreated group '");
				buf.append(group_name);
				buf.append("'\n");
				log.add(buf.toString());

				// now add the group owner

				String groupowner_name = group_name;

				User u = new User();

				u.setAdditionalInfo("Owner of group "
						+ group_name
						+ ", company: "
						+ (String) request.getSession().getAttribute(
								"groupWizardCompanyName")
						+ ", url: "
						+ (String) request.getSession().getAttribute(
								"groupWizardCompanyUrl"));
				u.setEmail(((String) ((DynaActionForm) form)
						.get("messageboxEmail")).trim());
				u.setFullName(group_name + " Group");
				u.setUserName(groupowner_name);
				u.setUnavailableTransferTo("messagebox");
				u.setPassword("a1b2c3d4");
				u.getOwnsGroups().add(group_name);
				u.getMemberOfGroups().add(domain);

				try {
					UserBean userBean = SpringUtils.getBean(request
							.getSession().getServletContext(), UserBean.class);
					userBean.createUser(u);

					AceLogger.Instance().log(
							AceLogger.INFORMATIONAL,
							AceLogger.USER_LOG,
							"User " + request.getUserPrincipal().getName()
									+ " created new webtalk user "
									+ groupowner_name);

					// append to wizard action log
					buf = new StringBuffer("\nCreated group owner '");
					buf.append(groupowner_name);
					buf.append("'\n");
					log.add(buf.toString());

					// now add and activate the feature

					Integer max_operators = (Integer) ((DynaActionForm) form)
							.get("maxOperators");

					FeatureBean featureBean = SpringUtils.getBean(request
							.getSession().getServletContext(),
							FeatureBean.class);

					Feature f = new Feature();

					f.setActive(true);
					f.setClassName(FeatureOperatorManagementAction.CLASSNAME);
					f.setName(groupowner_name);
					f.setDomain(domain);

					f.getParams().add(
							new FeatureParam("max-sessions",
									((Integer) ((DynaActionForm) form)
											.get("maxSessions")).toString()));
					f.getParams().add(
							new FeatureParam("max-queue-size", new Integer(
									max_operators.intValue() * 4).toString()));
					f.getParams().add(
							new FeatureParam("max-operators", max_operators
									.toString()));
					try {
						featureBean.createFeature(f);
						
						AceLogger.Instance().log(
								AceLogger.INFORMATIONAL,
								AceLogger.USER_LOG,
								"User " + request.getUserPrincipal().getName()
										+ " created active feature "
										+ groupowner_name);

						// append to wizard action log
						buf = new StringBuffer("\nCreated feature '");
						buf.append(groupowner_name);
						buf.append("'\n");
						log.add(buf.toString());

						// finally, activate the feature

						// notify the app server via RMI
						if (FeatureManagementAction.notifyAppServer(request,
								groupowner_name, "activate", null,
								"Create Feature/by-" + request.getUserPrincipal().getName()) == false) {
							errors.add(
									ActionErrors.GLOBAL_ERROR,
									new ActionError(
											"error.groupfeature.activate.failure",
											new String(groupowner_name),
											new String(group_name)));

							// append to wizard action log
							buf = new StringBuffer(
									"\n***  WARNING  ***  The data for the new group got added successfully, but the feature ('");
							buf.append(groupowner_name);
							buf.append("') for the new operator group could not be activated at the Ace Application Server. You can proceed with the wizard, but afterward you'll need to (re)start the Application Server before the new group '");
							buf.append(group_name);
							buf.append("' can be used.");
							buf.append("\n");
							log.add(buf.toString());
						}						
					} catch (Exception e) {
						errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
								"error.groupfeature.create.failure",
								new String(groupowner_name), new String(
										group_name)));

						AceLogger.Instance().log(
								AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								"GroupWizardGroupAddAction.execute()/Create Feature/by-"
										+ request.getUserPrincipal().getName() + ": "
										+ e.getMessage());

						// append to wizard action log
						buf = new StringBuffer(
								"\n***  ERROR  *** creating feature : The feature named '");
						buf.append(groupowner_name);
						buf.append("' for the new group '");
						buf.append(group_name);
						buf.append("' could not be created because a feature with that name already exists or there was a database error. If that feature name does already exist in the system, proceed with the wizard as you normally would. After finishing with the wizard, remove the old (presumably unused) feature named '");
						buf.append(groupowner_name);
						buf.append("' and add a new feature with the same name and with the parameter settings you want for this new group (remember to activate the new feature after adding it). Note, a group owner named '");
						buf.append(groupowner_name);
						buf.append("' has just been successfully added to the system for the new group '");
						buf.append(group_name);
						buf.append("', and the parameter settings for feature '");
						buf.append(groupowner_name);
						buf.append("' will be applied to the new group.");
						buf.append("\n");
						log.add(buf.toString());						
					}
				} catch (Exception e) {
					errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
							"error.groupowner.create.failure", new String(
									groupowner_name), new String(group_name)));

					AceLogger.Instance()
							.log(AceLogger.ERROR,
									AceLogger.SYSTEM_LOG,
									"GroupWizardGroupAddAction.execute()/Create Group Owner/by-"
											+ request.getUserPrincipal().getName() + ": "
											+ e.getMessage(), e);

					// append to wizard action log
					buf = new StringBuffer(
							"\n***  ERROR  *** creating group owner : The group owner named '");
					buf.append(groupowner_name);
					buf.append("' for the new group '");
					buf.append(group_name);
					buf.append("' could not be created because a user with that name already exists or there was a database error. If that user name already exists in the system, proceed with the wizard as you normally would. After finishing with the wizard, add the group owner and the feature for this new group yourself, using a unique name other than '");
					buf.append(groupowner_name);
					buf.append("'. Remember to activate the feature after adding it.");
					buf.append("\n");
					log.add(buf.toString());
				}
			} catch (Exception e) {

				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
						"error.group.create.failure"));

				AceLogger.Instance().log(
						AceLogger.ERROR,
						AceLogger.SYSTEM_LOG,
						"GroupWizardGroupAddAction.execute()/Create Group/by-"
								+ request.getUserPrincipal().getName() + ": " + e.getMessage());
			}
		}

		WebTalkRelatedTasks menu = new WebTalkRelatedTasks();
		request.setAttribute("menu", menu);

		if (submit.startsWith("Cancel") == false) {
			// add related tasks to the navigation bar

			menu.addLink(new LinkAttribute("List all groups", "list_groups"));
			menu.addLink(new LinkAttribute("List all features", "list_features"));
			menu.addLink(new LinkAttribute("Search users",
					"display_user_search"));
		}

		if (errors.isEmpty() == false) {
			saveErrors(request, errors);
			return mapping.getInputForward();
		}

		return mapping.findForward(submit);
	}

}
