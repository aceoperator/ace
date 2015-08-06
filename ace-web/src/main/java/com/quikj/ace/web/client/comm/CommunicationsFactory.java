/**
 * 
 */
package com.quikj.ace.web.client.comm;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestException;


/**
 * @author amit
 * 
 */
public class CommunicationsFactory {

	private static Server serverInstance = null;

	public static Server getServerCommunications() {
		if (serverInstance == null) {
			serverInstance = new ApplicationLayerImpl();

			ApplicationLayer application = (ApplicationLayer) serverInstance;

			TransportLayer transport = new TransportLayerImpl();
			application.setTransport(transport);
			transport.setApplication(application);
		}
		return serverInstance;
	}

	public static void sendMessageToServer(RequestBuilder builder)
			throws RequestException {
		// The following header is needed so that ios does not cache the request
		builder.setHeader("cache-control", "no-cache");
	
		builder.setTimeoutMillis(TransportLayer.RPC_TIMEOUT);
		builder.send();
	}
}
