package com.quikj.application.communicator.applications.webtalk.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

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

import com.quikj.ace.db.core.webtalk.vo.Feature;
import com.quikj.ace.db.core.webtalk.vo.FeatureParam;
import com.quikj.ace.db.webtalk.WebTalkException;
import com.quikj.ace.db.webtalk.model.FeatureBean;
import com.quikj.application.communicator.admin.controller.LinkAttribute;
import com.quikj.application.communicator.admin.controller.SpringUtils;
import com.quikj.server.framework.AceLogger;

/**
 * 
 * @author bhm
 */
public class FeatureOperatorManagementAction extends Action {

	private static final int PAUSE_DURATION = 60;
	public static final String CLASSNAME = "com.quikj.application.web.talk.feature.operator.Operator";

	public FeatureOperatorManagementAction() {
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) {
		FeatureOperatorManagementForm fform = (FeatureOperatorManagementForm) form;

		ActionErrors errors = new ActionErrors();

		FeatureBean featureBean = SpringUtils.getBean(request.getSession().getServletContext(), FeatureBean.class);
		ActionMessages messages = new ActionMessages();

		if (fform.getSubmit().equals("Find")) {
			try {
				Feature e = featureBean.findByName(fform.getName());
				fform.setActive(e.isActive());
				fform.setName(e.getName());

				HashMap<String, String> paramList = new HashMap<String, String>();
				for (FeatureParam param : e.getParams()) {
					paramList.put(param.getName(), param.getValue());
				}
				fform.setParamsList(paramList);

				if (e.isActive()) {
					request.setAttribute("featureStatus", "Active");
				} else {
					request.setAttribute("featureStatus", "Inactive");
				}

				boolean paused = false;
				String pausedUntilString = FeatureManagementAction.getFeatureParam(request, fform.getName(),
						"paused-until", errors, "Find/by-" + request.getUserPrincipal().getName());
				long pausedUntil = 0L;
				if (pausedUntilString != null) {
					pausedUntil = Long.parseLong(pausedUntilString);
					paused = new Date(pausedUntil).after(new Date());
				}

				request.setAttribute("featurePaused", Boolean.toString(paused));
				if (paused) {
					request.setAttribute("pausedUntil", new Date(pausedUntil).toString());
				}

				if (!e.getClassName().equals(CLASSNAME)) {
					// forward to generic feature form
					WebTalkRelatedTasks menu = new WebTalkRelatedTasks();
					menu.addLink(new LinkAttribute("Search users", "display_user_search"));
					menu.addLink(new LinkAttribute("Administer users", "display_user_management"));
					request.setAttribute("menu", menu);

					return mapping.findForward("feature_management");
				}
			} catch (WebTalkException e) {
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.feature.not.exist"));
			} catch (Exception e) {
				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
						"FeatureOperatorManagementAction.execute()/Find/by-" + request.getUserPrincipal().getName()
								+ ": " + e.getMessage());
			}
		} else if (fform.getSubmit().equals("Modify")) {
			Feature e = new Feature();
			e.setName(fform.getName());
			e.setClassName(CLASSNAME);
			e.setDomain(fform.getDomain());

			for (Entry<String, String> i : fform.getParamsList().entrySet()) {
				e.getParams().add(new FeatureParam(i.getKey(), i.getValue()));
			}

			try {
				featureBean.modifyFeature(e);
				AceLogger.Instance().log(AceLogger.INFORMATIONAL, AceLogger.USER_LOG,
						"User " + request.getUserPrincipal().getName() + " modified feature " + fform.getName());

				messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("message.feature.modified"));

				// notify app server via RMI
				if (featureBean.isFeatureActive(e.getName())) {
					if (FeatureManagementAction.notifyAppServer(request, e.getName(), "synch", errors,
							"Modify/by-" + request.getUserPrincipal().getName()) == true) {
						messages.add(ActionMessages.GLOBAL_MESSAGE,
								new ActionMessage("message.feature.appserver.updated"));
					}
				}
			} catch (WebTalkException ex) {
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.feature.not.exist"));
			} catch (Exception ex) {
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.db.failure"));

				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
						"FeatureOperatorManagementAction.execute()/Modify/by-" + request.getUserPrincipal().getName()
								+ ": " + ex.getMessage());
			}

			// forward control to the webtalk main menu
			if (!errors.isEmpty()) {
				saveErrors(request, errors);
			}

			saveMessages(request, messages);
			return mapping.findForward("webtalk_main_menu");
		} else if (fform.getSubmit().equals("Create")) {
			Feature e = new Feature();

			e.setActive(fform.isActive());
			e.setClassName(CLASSNAME);
			e.setName(fform.getName());
			e.setDomain(fform.getDomain());

			for (Entry<String, String> i : fform.getParamsList().entrySet()) {
				e.getParams().add(new FeatureParam(i.getKey(), i.getValue()));
			}

			try {
				featureBean.createFeature(e);
				AceLogger.Instance().log(AceLogger.INFORMATIONAL, AceLogger.USER_LOG,
						"User " + request.getUserPrincipal().getName() + " created feature " + fform.getName());

				// forward control to the webtalk main menu
				messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("message.feature.created"));
				saveMessages(request, messages);
				return mapping.findForward("webtalk_main_menu");
			} catch (Exception ex) {
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.feature.create.failure"));

				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
						"FeatureOperatorManagementAction.execute()/Create/by-" + request.getUserPrincipal().getName()
								+ ": " + ex.getMessage());
			}
		} else if (fform.getSubmit().equals("Delete")) {
			try {
				featureBean.deleteFeature(fform.getName());

				AceLogger.Instance().log(AceLogger.INFORMATIONAL, AceLogger.USER_LOG,
						"User " + request.getUserPrincipal().getName() + " deleted feature " + fform.getName());

				messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("message.feature.deleted"));

				// notify app server via RMI
				if (FeatureManagementAction.notifyAppServer(request, fform.getName(), "deactivate", errors,
						"Delete/by-" + request.getUserPrincipal().getName()) == true) {
					messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("message.feature.appserver.updated"));
				}

				// forward control to the webtalk main menu
				if (!errors.isEmpty()) {
					saveErrors(request, errors);
				}
				saveMessages(request, messages);
				return mapping.findForward("webtalk_main_menu");
			} catch (WebTalkException e) {
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.feature.not.exist"));
			} catch (Exception e) {
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.db.failure"));

				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
						"FeatureOperatorManagementAction.execute()/Delete/by-" + request.getUserPrincipal().getName()
								+ ": " + e.getMessage());
			}
		} else if (fform.getSubmit().equals("Activate")) {
			try {
				featureBean.setActive(fform.getName(), true);

				AceLogger.Instance().log(AceLogger.INFORMATIONAL, AceLogger.USER_LOG,
						"User " + request.getUserPrincipal().getName() + " activated feature " + fform.getName());

				messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("message.feature.modified"));

				// notify app server via RMI
				if (FeatureManagementAction.notifyAppServer(request, fform.getName(), "activate", errors,
						"Activate/by-" + request.getUserPrincipal().getName()) == true) {
					messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("message.feature.appserver.updated"));
				}

			} catch (WebTalkException e) {
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.feature.not.exist"));
			} catch (Exception e) {
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.db.failure"));

				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
						"FeatureOperatorManagementAction.execute()/Activate/by-" + request.getUserPrincipal().getName()
								+ ": " + e.getMessage());
			}

			// forward control to the webtalk main menu
			if (!errors.isEmpty()) {
				saveErrors(request, errors);
			}
			saveMessages(request, messages);
			return mapping.findForward("webtalk_main_menu");
		} else if (fform.getSubmit().equals("Deactivate")) {
			try {
				featureBean.setActive(fform.getName(), false);

				AceLogger.Instance().log(AceLogger.INFORMATIONAL, AceLogger.USER_LOG,
						"User " + request.getUserPrincipal().getName() + " deactivated feature " + fform.getName());

				messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("message.feature.modified"));

				// notify app server via RMI
				if (FeatureManagementAction.notifyAppServer(request, fform.getName(), "deactivate", errors,
						"Deactivate/by-" + request.getUserPrincipal().getName())) {
					messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("message.feature.appserver.updated"));
				}
			} catch (WebTalkException e) {
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.feature.not.exist"));
			} catch (Exception e) {
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.db.failure"));

				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
						"FeatureOperatorManagementAction.execute()/Deactivate/by-"
								+ request.getUserPrincipal().getName() + ": " + e.getMessage());
			}

			// forward control to the webtalk main menu
			if (!errors.isEmpty()) {
				saveErrors(request, errors);
			}
			saveMessages(request, messages);
			return mapping.findForward("webtalk_main_menu");
		} else if (fform.getSubmit().equals("Pause")) {
			try {
				if (FeatureManagementAction.setFeatureParam(request, fform.getName(), "pause-for",
						Long.toString(PAUSE_DURATION), errors, "Pause/by-" + request.getUserPrincipal().getName())) {
					AceLogger.Instance().log(AceLogger.INFORMATIONAL, AceLogger.USER_LOG,
							"User " + request.getUserPrincipal().getName() + " paused feature " + fform.getName());
					messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("message.feature.modified"));
				}
			} catch (WebTalkException e) {
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.feature.not.exist"));
			} catch (Exception e) {
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.db.failure"));

				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
						"FeatureOperatorManagementAction.execute()/Pause/by-" + request.getUserPrincipal().getName()
								+ ": " + e.getMessage());
			}

			if (!errors.isEmpty()) {
				saveErrors(request, errors);
			}
			saveMessages(request, messages);
			return mapping.findForward("webtalk_main_menu");
		} else if (fform.getSubmit().equals("Resume")) {
			try {
				if (FeatureManagementAction.setFeatureParam(request, fform.getName(), "pause-for", "0", errors,
						"Resume/by-" + request.getUserPrincipal().getName())) {

					AceLogger.Instance().log(AceLogger.INFORMATIONAL, AceLogger.USER_LOG,
							"User " + request.getUserPrincipal().getName() + " resumed feature " + fform.getName());

					messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("message.feature.modified"));
				}

			} catch (WebTalkException e) {
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.feature.not.exist"));
			} catch (Exception e) {
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.db.failure"));

				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
						"FeatureOperatorManagementAction.execute()/Resume/by-" + request.getUserPrincipal().getName()
								+ ": " + e.getMessage());
			}

			if (!errors.isEmpty()) {
				saveErrors(request, errors);
			}
			saveMessages(request, messages);
			return mapping.findForward("webtalk_main_menu");
		}

		// forward control to the webtalk main menu
		if (!errors.isEmpty()) {
			saveErrors(request, errors);
		}
		saveMessages(request, messages);

		// add related tasks to the navigation bar
		WebTalkRelatedTasks menu = new WebTalkRelatedTasks();
		menu.addLink(new LinkAttribute("Search users", "display_user_search"));
		menu.addLink(new LinkAttribute("Administer users", "display_user_management"));
		menu.addLink(new LinkAttribute("List all groups", "list_groups"));
		menu.addLink(new LinkAttribute("Administer groups", "display_group_management"));
		menu.addLink(new LinkAttribute("List all features", "list_features"));
		request.setAttribute("menu", menu);

		return mapping.getInputForward();
	}
}
