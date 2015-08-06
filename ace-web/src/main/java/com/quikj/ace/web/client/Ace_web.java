package com.quikj.ace.web.client;

import java.util.HashMap;
import java.util.Set;
import java.util.logging.Logger;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.UmbrellaException;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.quikj.ace.web.client.comm.CommunicationsFactory;
import com.quikj.ace.web.client.presenter.MainPanelPresenter;

public class Ace_web implements EntryPoint {

	private static final String DEFAULT_PROFILE_NAME = "default-operator";

	private Logger logger;

	private String browserType;

	@Override
	public void onModuleLoad() {
		logger = Logger.getLogger(getClass().getName());

		GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {
			@Override
			public void onUncaughtException(Throwable e) {
				StringBuilder builder = new StringBuilder();
				if (e instanceof UmbrellaException) {
					UmbrellaException u = (UmbrellaException) e;
					Set<Throwable> causes = u.getCauses();
					for (Throwable cause : causes) {
						builder.append("Cause: " + cause.getClass().getName()
								+ " - " + cause.getMessage() + ", ");
					}
				}

				logger.severe("Exception "
						+ e.getClass().getName()
						+ " occured - "
						+ e.getMessage()
						+ " ("
						+ (e.getCause() != null ? e.getCause().getClass()
								.getName()
								+ ": " + e.getCause().getMessage() : "")
						+ "), Causes: " + builder.toString());
				e.printStackTrace();
			}
		});

		String profileName = Window.Location
				.getParameter(ClientProperties.PROFILE);
		if (profileName == null) {
			logger.fine("Profile name not specified. Going to use "
					+ DEFAULT_PROFILE_NAME);
			profileName = DEFAULT_PROFILE_NAME;
		}

		browserType = MainPanelPresenter.getInstance().getBrowserType();
		RequestBuilder builder = AceOperatorService.Util.getInstance()
				.getProfile(profileName, browserType,
						new AsyncCallback<HashMap<String, String>>() {

							@Override
							public void onSuccess(HashMap<String, String> result) {
								init(result);
							}

							@Override
							public void onFailure(Throwable caught) {
								logger.severe("The specified profile does not exist on the server."
										+ " Going to use standard URL properties and defaults");
								init(null);
							}
						});

		try {
			CommunicationsFactory.sendMessageToServer(builder);
		} catch (RequestException e) {
			logger.severe("Error sending message to the server - "
					+ e.getMessage());
		}
	}

	private void init(HashMap<String, String> properties) {
		// Load the properties from the URL
		boolean toContinue = ClientProperties.getInstance().loadProperties(
				browserType, properties);
		if (!toContinue) {
			return;
		}

		// launch the controller (which takes over)
		ApplicationController.getInstance().launch();
	}
}
