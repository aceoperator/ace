/*
 * ActionEvent.java
 *
 * Created on March 8, 2002, 1:48 AM
 */

package com.quikj.application.web.talk.plugin;

import java.util.ArrayList;

import com.quikj.server.framework.AceMessageInterface;

/**
 * 
 * @author amit
 */
public class ActionEvent implements AceMessageInterface {

	private ArrayList actionList = new ArrayList();

	private Object userParm;

	/** Creates a new instance of ActionEvent */
	protected ActionEvent(Object user_parm) {
		userParm = user_parm;
	}

	public int actionListSize() {
		return actionList.size();
	}

	public void addAction(EndPointActionInterface action) {
		actionList.add(action);
	}

	public EndPointActionInterface[] getActionList() {
		int size = actionList.size();
		EndPointActionInterface[] array = new EndPointActionInterface[size];
		for (int i = 0; i < array.length; i++) {
			array[i] = (EndPointActionInterface) actionList.get(i);
		}

		return array;
		// return (EndPointActionInterface[])actionList.toArray();
	}

	public Object getUserParm() {
		return userParm;
	}
	public String messageType() {
		return "ApplicationTalkActionEvent";
	}
}
