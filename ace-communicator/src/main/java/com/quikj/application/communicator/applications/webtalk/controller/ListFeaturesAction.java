/*
 * ListFeaturesAction.java
 *
 */

package com.quikj.application.communicator.applications.webtalk.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.quikj.ace.db.core.webtalk.vo.Feature;
import com.quikj.ace.db.webtalk.model.FeatureBean;
import com.quikj.application.communicator.admin.controller.LinkAttribute;
import com.quikj.application.communicator.admin.controller.SpringUtils;
import com.quikj.server.framework.AceLogger;

/**
 * 
 * @author bhm
 */
public class ListFeaturesAction extends Action {

	public ListFeaturesAction() {
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		ActionErrors errors = new ActionErrors();

		FeatureBean featureBean = SpringUtils.getBean(request.getSession()
				.getServletContext(), FeatureBean.class);

		try {
			List<Feature> features = featureBean.listFeatures(null);

			// store the list result items for the jsp
			ArrayList<HashMap<String, String>> nameList = new ArrayList<HashMap<String, String>>();
			for (Feature feature : features) {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("name", feature.getName());
				map.put("submit", "Find");
				map.put("domain", feature.getDomain());

				if (feature.isActive()) {
					map.put("status", "active");
				} else {
					map.put("status", "inactive");
				}

				if (feature.getClassName().equals(
						FeatureOperatorManagementAction.CLASSNAME)) {
					map.put("forward", "feature_operator_management");
				} else {
					map.put("forward", "feature_management");
				}

				nameList.add(map);
			}

			request.setAttribute("features", nameList);

		} catch (Exception e) {
			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
					"error.db.failure"));

			AceLogger.Instance().log(
					AceLogger.ERROR,
					AceLogger.SYSTEM_LOG,
					"ListFeaturesAction.execute()/by-" + request.getUserPrincipal().getName()
							+ ": " + e.getMessage());
		}

		if (!errors.isEmpty()) {
			saveErrors(request, errors);
		}

		// add related tasks to the navigation bar
		WebTalkRelatedTasks menu = new WebTalkRelatedTasks();
		menu.addLink(new LinkAttribute("Administer operator features",
				"display_feature_operator_management"));
		menu.addLink(new LinkAttribute("Administer other features",
				"display_feature_management"));
		menu.addLink(new LinkAttribute("Administer users",
				"display_user_management"));
		menu.addLink(new LinkAttribute("Search users", "display_user_search"));
		request.setAttribute("menu", menu);

		return mapping.getInputForward();
	}
}