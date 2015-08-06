/*
 * FeatureInterface.java
 *
 * Created on April 23, 2002, 2:08 AM
 */

package com.quikj.application.web.talk.plugin;

import java.util.Map;

/**
 * 
 * @author amit
 */
public interface FeatureInterface {
	public void clearStatsCounts();

	public void dispose();

	public String getUserName();

	public boolean init(String name, Map<?,?> params);

	public void resynchParam(Map<?,?> params);

	public void start();
}
