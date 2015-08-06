/*
 * DropCustomerAction.java
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

import com.quikj.ace.db.core.webtalk.vo.Feature;
import com.quikj.ace.db.core.webtalk.vo.Group;
import com.quikj.ace.db.webtalk.WebTalkException;
import com.quikj.ace.db.webtalk.model.FeatureBean;
import com.quikj.ace.db.webtalk.model.GroupBean;
import com.quikj.ace.db.webtalk.model.UserBean;
import com.quikj.application.communicator.admin.controller.SpringUtils;
import com.quikj.server.framework.AceLogger;

/**
 * 
 * @author bhm
 */
public class DropCustomerAction extends Action {
	public DropCustomerAction() {
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		ActionErrors errors = new ActionErrors();

		String domain = (String) ((DynaActionForm) form).get("domain");
		if ((domain == null) || (domain.length() == 0)) {
			// did not get here through normal sequence

			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
					"error.sequence"));
			saveErrors(request, errors);

			return mapping.findForward("display_drop_customer_intro");
		}

		String submit = (String) ((DynaActionForm) form).get("submit");

		if (submit.startsWith("Remove")) {
			// create action log
			ArrayList<String> log = new ArrayList<String>();

			StringBuffer buf = new StringBuffer(
					"Removing data associated with customer:  ");
			buf.append(domain);
			buf.append("\n");
			log.add(buf.toString());
			log.add("\n");

			request.setAttribute("dropCustomerLog", log);

			UserBean userBean = SpringUtils.getBean(request.getSession()
					.getServletContext(), UserBean.class);

			List<String> operators = null;
			List<String> owners = null;
			try {
				operators = userBean.findMembersByGroupDomain(domain);
				owners = userBean.findOwnersByGroupDomain(domain);
			} catch (Exception e) {
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
						"error.customer.delete.finddata"));

				saveErrors(request, errors);

				// append to wizard action log
				buf = new StringBuffer(
						"\n***  ERROR  ***     Error encountered while attempting to find data related to this customer. Nothing has been deleted. Please check system logs, correct the problem and try using this wizard again later.");
				buf.append("\n");
				log.add(buf.toString());

				// log the error(s)
				AceLogger.Instance().log(
						AceLogger.ERROR,
						AceLogger.SYSTEM_LOG,
						"DropCustomerAction.execute()/by-" + request.getUserPrincipal().getName()
								+ ": Error finding data in domain " + domain
								+ ": " + e.getMessage(), e);
			}

			// delete the operators
			for (String name : operators) {
				try {
					userBean.removeUser(name);

					// append to wizard action log
					buf = new StringBuffer("Deleted operator '");
					buf.append(name);
					buf.append("'\n");
					log.add(buf.toString());

					AceLogger.Instance().log(
							AceLogger.INFORMATIONAL,
							AceLogger.USER_LOG,
							"User " + request.getUserPrincipal().getName()
									+ " deleted webtalk user " + name);

				} catch (Exception e) {

					errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
							"error.customer.delete.operator", new String(name)));

					// append to wizard action log
					buf = new StringBuffer(
							"\n***  ERROR  ***     Error encountered while attempting to delete operator '");
					buf.append(name);
					buf.append("'. You'll have to delete it yourself.");
					buf.append("\n");
					log.add(buf.toString());

					// log the error
					AceLogger.Instance().log(
							AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							"DropCustomerAction.execute()/by-"
									+ request.getUserPrincipal().getName()
									+ ": Error deleting webtalk user "
									+ e.getMessage(), e);
				}
			}

			// delete the features & group owners

			FeatureBean featureBean = SpringUtils.getBean(request.getSession()
					.getServletContext(), FeatureBean.class);

			for (String name : owners) {
				// delete feature if present

				try {
					featureBean.findByName(name);
				}
				catch (WebTalkException e) {
					// The feature does not exist
					continue;
				}
				
				try {
					featureBean.deleteFeature(name);

					// notify app server via RMI
					if (FeatureManagementAction.notifyAppServer(request, name,
							"deactivate", errors,
							"Delete/by-" + request.getUserPrincipal().getName())) {
						// append to wizard action log
						buf = new StringBuffer("Deactivated feature '");
						buf.append(name);
						buf.append("'\n");
						log.add(buf.toString());
					}

					// append to wizard action log
					buf = new StringBuffer("Deleted feature '");
					buf.append(name);
					buf.append("'\n");
					log.add(buf.toString());

					AceLogger.Instance().log(
							AceLogger.INFORMATIONAL,
							AceLogger.USER_LOG,
							"User " + request.getUserPrincipal().getName() + " deleted feature "
									+ name);
				} catch (WebTalkException e) {
					errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
							"error.customer.delete.feature", new String(name)));

					// append to wizard action log
					buf = new StringBuffer(
							"\n***  ERROR  ***     Error encountered while attempting to delete feature '");
					buf.append(name);
					buf.append("'. You'll have to delete it yourself.");
					buf.append("\n");
					log.add(buf.toString());

					// log the error
					AceLogger.Instance().log(
							AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							"DropCustomerAction.execute()/by-"
									+ request.getUserPrincipal().getName()
									+ ": Error deleting feature " + name + ": "
									+ e.getMessage());
				}

				// delete group owner
				try {
					userBean.removeUser(name);

					// append to wizard action log
					buf = new StringBuffer("Deleted group owner '");
					buf.append(name);
					buf.append("', groups owned by this user and canned messages specific to those groups\n");
					log.add(buf.toString());

					AceLogger
							.Instance()
							.log(AceLogger.INFORMATIONAL,
									AceLogger.USER_LOG,
									"User "
											+ request.getUserPrincipal().getName()
											+ " deleted webtalk user "
											+ name
											+ ", groups owned by this user and canned messages specific to those groups");
				} catch (Exception e) {
					errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
							"error.customer.delete.owner", name));

					// append to wizard action log
					buf = new StringBuffer(
							"\n***  ERROR  ***     Error encountered while attempting to delete group owner '");
					buf.append(name);
					buf.append("', groups owned by this user and canned messages specific to those groups. You'll have to delete this group owner yourself.");
					buf.append("\n");
					log.add(buf.toString());

					// log the error
					AceLogger
							.Instance()
							.log(AceLogger.ERROR,
									AceLogger.SYSTEM_LOG,
									"DropCustomerAction.execute()/by-"
											+ request.getUserPrincipal().getName()
											+ ": Error deleting webtalk user "
											+ name
											+ ", groups owned by this user and canned messages specific to those groups: "
											+ e.getMessage(), e);
				}
			}

			// get a list of any stray users
			try {
				List<String> strayUsers = userBean.listUsersByDomain(domain);

				// delete any stray users
				for (String name : strayUsers) {
					try {
						userBean.removeUser(name);

						// append to wizard action log
						buf = new StringBuffer("Deleted stray user '");
						buf.append(name);
						buf.append("', any groups owned by this user and canned messages specific to those groups\n");
						log.add(buf.toString());

						AceLogger
								.Instance()
								.log(AceLogger.INFORMATIONAL,
										AceLogger.USER_LOG,
										"User "
												+ request.getUserPrincipal().getName()
												+ " deleted webtalk user "
												+ name
												+ ", any groups owned by this user and canned messages specific to those groups");

					} catch (Exception e) {

						errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
								"error.customer.delete.strayuser", new String(
										name)));

						// append to wizard action log
						buf = new StringBuffer(
								"\n***  ERROR  ***     Error encountered while attempting to delete stray user '");
						buf.append(name);
						buf.append("', any groups owned by this user and canned messages specific to those groups. You'll have to delete this user yourself.");
						buf.append("\n");
						log.add(buf.toString());

						// log the error
						AceLogger
								.Instance()
								.log(AceLogger.ERROR,
										AceLogger.SYSTEM_LOG,
										"DropCustomerAction.execute()/by-"
												+ request.getUserPrincipal().getName()
												+ ": Error deleting webtalk user "
												+ name
												+ ", any groups owned by this user and canned messages specific to those groups: "
												+ e.getMessage(), e);

					}
				}

			} catch (Exception e) {
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
						"error.customer.delete.strayusers"));

				// append to wizard action log
				buf = new StringBuffer(
						"\n***  ERROR  ***     Error encountered while attempting to find stray users related to this customer. Search remaining Ace Operator users, and delete any that were associated with this customer.");
				buf.append("\n");
				log.add(buf.toString());

				AceLogger.Instance().log(
						AceLogger.ERROR,
						AceLogger.SYSTEM_LOG,
						"DropCustomerAction.execute()/by-" + request.getUserPrincipal().getName()
								+ ": Error finding data in domain " + domain
								+ ": " + "stray users - " + e.getMessage(), e);
			}

			// get a list of any stray groups
			GroupBean groupBean = SpringUtils.getBean(request.getSession()
					.getServletContext(), GroupBean.class);

			List<Group> strayGroups = groupBean.listGroups(domain);
			for (Group group : strayGroups) {
				try {
					groupBean.deleteGroup(group.getName());

					// append to wizard action log
					buf = new StringBuffer("Deleted stray group '");
					buf.append(group.getName());
					buf.append("' and canned messages specific to this group\n");
					log.add(buf.toString());

					AceLogger
							.Instance()
							.log(AceLogger.INFORMATIONAL,
									AceLogger.USER_LOG,
									"User "
											+ request.getUserPrincipal().getName()
											+ " deleted stray group "
											+ group.getName()
											+ " and canned messages specific to this group");
				} catch (WebTalkException e) {
					errors.add(ActionErrors.GLOBAL_ERROR,
							new ActionError("error.customer.delete.straygroup",
									group.getName()));

					// append to wizard action log
					buf = new StringBuffer(
							"\n***  ERROR  ***     Error encountered while attempting to delete stray group '");
					buf.append(group.getName());
					buf.append("' and canned messages specific to this group. You'll have to delete this group yourself.");
					buf.append("\n");
					log.add(buf.toString());

					// log the error
					AceLogger
							.Instance()
							.log(AceLogger.ERROR,
									AceLogger.SYSTEM_LOG,
									"DropCustomerAction.execute()/by-"
											+ request.getUserPrincipal().getName()
											+ ": Error deleting stray group "
											+ group.getName()
											+ " and canned messages specific to this group: "
											+ e.getMessage());
				}
			}

			// get a list of any stray features
			List<Feature> strayFeatures = featureBean.listFeatures(domain);

			// delete and deactivate

			for (Feature feature : strayFeatures) {
				try {
					featureBean.deleteFeature(feature.getName());

					// notify app server via RMI
					if (FeatureManagementAction.notifyAppServer(request, feature.getName(),
							"deactivate", errors,
							"Delete/by-" + request.getUserPrincipal().getName()) == true) {
						// append to wizard action log
						buf = new StringBuffer("Deactivated stray feature '");
						buf.append(feature.getName());
						buf.append("'\n");
						log.add(buf.toString());
					}

					// append to wizard action log
					buf = new StringBuffer("Deleted stray feature '");
					buf.append(feature.getName());
					buf.append("'\n");
					log.add(buf.toString());

					AceLogger.Instance().log(
							AceLogger.INFORMATIONAL,
							AceLogger.USER_LOG,
							"User " + request.getUserPrincipal().getName()
									+ " deleted stray feature " + feature.getName());
				
				} catch (WebTalkException e) {
					errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
							"error.customer.delete.strayfeature", new String(
									feature.getName())));

					// append to wizard action log
					buf = new StringBuffer(
							"\n***  ERROR  ***     Error encountered while attempting to delete stray feature '");
					buf.append(feature.getName());
					buf.append("'. You'll have to delete this feature yourself.");
					buf.append("\n");
					log.add(buf.toString());

					// log the error
					AceLogger.Instance().log(
							AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							"DropCustomerAction.execute()/by-"
									+ request.getUserPrincipal().getName()
									+ ": Error deleting stray feature " + feature.getName()
									+ ": " + e.getMessage());
				}
			}
		}

		if (!errors.isEmpty()) {
			saveErrors(request, errors);
		}

		return mapping.findForward(submit);
	}
}
