/**
 * 
 */
package com.quikj.ace.web.client.view;

import com.quikj.ace.messages.vo.adapter.GroupInfo;
import com.quikj.ace.messages.vo.talk.CallPartyElement;
import com.quikj.ace.web.client.AudioSettings;
import com.quikj.ace.web.client.ChatSettings;
import com.quikj.ace.web.client.presenter.UserInfoPresenter;

/**
 * @author beckie
 * 
 */
public interface UserInfoPanel {

	public void setPresenter(UserInfoPresenter presenter);

	public void setValues(CallPartyElement userInfo,
			AudioSettings audioSettings, ChatSettings callSettings);

	public void reset();

	public void setGroupInfo(GroupInfo[] groupInfo);

	public void showChangePassword();

	public void lockEnabled(boolean enabled);

	public void showMyInfo();
}
