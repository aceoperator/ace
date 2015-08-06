/*
 * GatekeeperInterface.java
 *
 * Created on November 5, 2003, 7:31 AM
 */

package com.quikj.application.web.talk.plugin;

import com.quikj.server.app.EndPointInterface;

/**
 * 
 * @author amit
 */
public interface GatekeeperInterface {
	public boolean allow(EndPointInterface ep, EndPointInfo info);
}
