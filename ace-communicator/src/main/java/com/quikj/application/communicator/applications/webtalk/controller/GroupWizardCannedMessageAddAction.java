/*
 * GroupWizardCannedMessageAddAction.java
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

import com.quikj.ace.db.core.webtalk.vo.CannedMessage;
import com.quikj.ace.db.core.webtalk.vo.Group;
import com.quikj.ace.db.webtalk.model.CannedMessageBean;
import com.quikj.ace.db.webtalk.model.GroupBean;
import com.quikj.application.communicator.admin.controller.LinkAttribute;
import com.quikj.application.communicator.admin.controller.SpringUtils;
import com.quikj.server.framework.AceLogger;

/**
 * 
 * @author bhm
 */
public class GroupWizardCannedMessageAddAction extends Action {

	public GroupWizardCannedMessageAddAction() {
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws UnsupportedEncodingException {
		GroupWizardCannedMessageForm cform = (GroupWizardCannedMessageForm) form;

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

		String submit = cform.getSubmit();

		if (submit.startsWith("Add")) {
			String domain = (String) request.getSession().getAttribute(
					"groupWizardDomain");

			ArrayList<LabelValueBean> list = populateGroups(request, domain);
			
			cform.setUserGroups(list);

			CannedMessageBean cannedBean = SpringUtils.getBean(request
					.getSession().getServletContext(), CannedMessageBean.class);

			CannedMessage e = new CannedMessage();

			e.setGroupName(URLDecoder.decode(cform.getGroup(), "UTF-8"));
			e.setDescription(cform.getDescription());
			e.setMessage(cform.getContent());

			try {
				cannedBean.create(e);

				AceLogger.Instance().log(
						AceLogger.INFORMATIONAL,
						AceLogger.USER_LOG,
						"User " + request.getUserPrincipal().getName()
								+ " created webtalk canned message "
								+ e.getDescription());

				ActionMessages messages = new ActionMessages();
				messages.add(ActionMessages.GLOBAL_MESSAGE,
						new ActionMessage("message.groupmessage.x.created",
								new String(cform.getDescription())));
				saveMessages(request, messages);

				// append to wizard action log
				StringBuffer buf = new StringBuffer(
						"\nCreated canned message '");
				buf.append(cform.getDescription());
				buf.append("'\n");
				log.add(buf.toString());

			} catch (Exception ex) {
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
						"error.groupmessage.create.failure"));

				AceLogger.Instance().log(
						AceLogger.ERROR,
						AceLogger.SYSTEM_LOG,
						"GroupWizardCannedMessageAddAction.execute()/Create/by-"
								+ request.getUserPrincipal().getName() + ": " + ex.getMessage(),
						ex);
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
			menu.addLink(new LinkAttribute("Search canned messages",
					"display_canned_message_search"));
		}

		if (errors.isEmpty() == false) {
			saveErrors(request, errors);
			return mapping.getInputForward();
		}

		if (submit.startsWith("Finished") == true) {
			// nothing new to add, we're at the end of the wizard
		}

		return mapping.findForward(submit);
	}

	protected static ArrayList<LabelValueBean> populateGroups(
			HttpServletRequest request, String domain)
			throws UnsupportedEncodingException {
		GroupBean groupBean = SpringUtils.getBean(request.getSession()
				.getServletContext(), GroupBean.class);

		List<Group> group_list = groupBean.listGroups(domain);

		ArrayList<LabelValueBean> list = new ArrayList<LabelValueBean>();
		for (Group group : group_list) {
			if (!group.getName().equals(domain)) {
				list.add(new LabelValueBean(group.getName(), URLEncoder
						.encode(group.getName(), "UTF-8")));
			} else {
				// TODO is this logic right? Revisit
				list.add(
						0,
						new LabelValueBean("All of this customer's groups",
								URLEncoder.encode(group.getName(), "UTF-8")));
			}
		}
		return list;
	}

}
