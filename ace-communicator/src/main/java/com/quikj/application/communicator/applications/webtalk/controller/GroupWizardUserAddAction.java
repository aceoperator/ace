/*
 * GroupWizardUserAddAction.java
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

import com.quikj.ace.db.core.webtalk.vo.Group;
import com.quikj.ace.db.core.webtalk.vo.User;
import com.quikj.ace.db.webtalk.model.GroupBean;
import com.quikj.ace.db.webtalk.model.UserBean;
import com.quikj.application.communicator.admin.controller.LinkAttribute;
import com.quikj.application.communicator.admin.controller.SpringUtils;
import com.quikj.server.framework.AceLogger;

/**
 * 
 * @author bhm
 */
public class GroupWizardUserAddAction extends Action {

	public GroupWizardUserAddAction() {
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws UnsupportedEncodingException {
		UserManagementForm uform = (UserManagementForm) form;

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

		String submit = uform.getSubmit();

		if (submit.equals("Create Operator")) {
			String domain = (String) request.getSession().getAttribute(
					"groupWizardDomain");

			GroupBean groupBean = SpringUtils.getBean(request.getSession()
					.getServletContext(), GroupBean.class);

			List<Group> groupList = groupBean.listGroups(domain);
			ArrayList<LabelValueBean> list = new ArrayList<LabelValueBean>();
			for (Group group : groupList) {
				if (!group.equals(domain)) {
					list.add(new LabelValueBean(group.getName(), URLEncoder
							.encode(group.getName(), "UTF-8")));
				}
			}

			uform.setUserGroups(list);

			User e = new User();

			e.setAdditionalInfo(uform.getAdditionalInfo());
			e.setEmail(uform.getAddress());
			e.setFullName(uform.getFullName());
			e.setUserName(uform.getName());
			e.setUnavailableTransferTo("messagebox");
			e.setGatekeeper(uform.getGatekeeper());
			e.setPassword(uform.getPassword());
			e.setChangePassword(uform.isChangePassword());
			e.getMemberOfGroups().add(domain);

			Object[] ogroups = uform.getBelongsToGroups();
			if (ogroups != null) {
				for (int i = 0; i < ogroups.length; i++) {
					if (ogroups[i] != null) {
						String decodedGroup = URLDecoder.decode(
								(String) ogroups[i], "UTF-8");
						e.getMemberOfGroups().add(decodedGroup);
					}
				}
			}

			try {
				UserBean userBean = SpringUtils.getBean(request.getSession()
						.getServletContext(), UserBean.class);
				userBean.createUser(e);

				AceLogger.Instance().log(
						AceLogger.INFORMATIONAL,
						AceLogger.USER_LOG,
						"User " + request.getUserPrincipal().getName() + " created webtalk user "
								+ uform.getName());

				ActionMessages messages = new ActionMessages();
				messages.add(ActionMessages.GLOBAL_MESSAGE,
						new ActionMessage("message.operator.x.created",
								new String(uform.getName())));
				saveMessages(request, messages);

				// append to wizard action log
				StringBuffer buf = new StringBuffer("\nCreated operator '");
				buf.append(uform.getName());
				buf.append("'\n");
				log.add(buf.toString());
			} catch (Exception ex) {

				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
						"error.account.create.failure"));

				AceLogger.Instance().log(
						AceLogger.ERROR,
						AceLogger.SYSTEM_LOG,
						"GroupWizardUserAddAction.execute()/Create/by-"
								+ request.getUserPrincipal().getName() + ": "
								+ e.getMemberOfGroups(), ex);
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

		if (submit.startsWith("Finished") == true) {
			menu.addLink(new LinkAttribute("Search canned messages",
					"display_canned_message_search"));
		}

		return mapping.findForward(submit);
	}

}
