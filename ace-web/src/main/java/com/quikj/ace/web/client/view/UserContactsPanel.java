/**
 * 
 */
package com.quikj.ace.web.client.view;

import java.util.List;

import com.quikj.ace.web.client.presenter.UserContactsPresenter;

/**
 * @author beckie
 * 
 */
public interface UserContactsPanel {

	public void addContact(UserContact contact);

	public void addContacts(List<UserContact> contacts);

	public void modifyContact(String user, int callCount);

	public void removeContact(String user);

	public void setPresenter(UserContactsPresenter presenter);

	public List<UserContact> getOnlineContacts();
}
