package com.quikj.server.app.adapter;

public class AppServerAdapterMeasurements {

	private int connectCountEnd = 0;
	private int disconnectCountEnd = 0;
	private int incomingMessageCountEnd = 0;
	private int profileRequestCountEnd = 0;
	private int connectCountStart = 0;
	private int disconnectCountStart = 0;
	private int incomingMessageCountStart = 0;
	private int profileRequestCountStart = 0;
	private int listCannedMsgCountStart = 0;
	private int listCannedMsgCountEnd = 0;
	private int getParamCountStart = 0;
	private int getParamCountEnd = 0;
	private int getGroupInfoCountStart = 0;
	private int getGroupInfoCountEnd = 0;
	private int getSecurityQuestionsCountStart = 0;
	private int getSecurityQuestionsCountEnd = 0;
	private int resetPasswordCountStart = 0;
	private int resetPasswordCountEnd = 0;
	private int recoverLostUsernameCountStart = 0;
	private int recoverLostUsernameCountEnd = 0;

	public AppServerAdapterMeasurements() {
	}

	public void formatMeasurements(StringBuffer buffer) {
		formatElement(buffer, "# connects: ", connectCountStart,
				connectCountEnd);
		formatElement(buffer, "# disconnects: ", disconnectCountStart,
				disconnectCountEnd);
		formatElement(buffer, "# incoming: ", incomingMessageCountStart,
				incomingMessageCountEnd);
		formatElement(buffer, "# profile access: ", profileRequestCountStart,
				profileRequestCountEnd);
		formatElement(buffer, "# canned message: ", listCannedMsgCountStart,
				listCannedMsgCountEnd);
		formatElement(buffer, "# param: ", getParamCountStart,
				getParamCountEnd);
		formatElement(buffer, "# group info: ", getGroupInfoCountStart,
				getGroupInfoCountEnd);
		formatElement(buffer, "# security questions: ",
				getSecurityQuestionsCountStart, getSecurityQuestionsCountEnd);
		formatElement(buffer, "# reset password: ", resetPasswordCountStart,
				resetPasswordCountEnd);
		formatElement(buffer, "# recover username: ",
				recoverLostUsernameCountStart, recoverLostUsernameCountEnd);
	}

	private void formatElement(StringBuffer buffer, String label,
			int startCount, int endCount) {
		if (buffer.length() > 0) {
			buffer.append("\n");
		}
		buffer.append(label);
		buffer.append(startCount);
		buffer.append(":");
		buffer.append(endCount);
	}

	public void incrementConnectStartCount() {
		connectCountStart++;
	}

	public void incrementDisconnectStartCount() {
		disconnectCountStart++;
	}

	public void incrementIncomingMessageStartCount() {
		incomingMessageCountStart++;
	}

	public void incrementProfileRequestStartCount() {
		profileRequestCountStart++;
	}
	
	public void incrementConnectEndCount() {
		connectCountEnd++;
	}

	public void incrementDisconnectEndCount() {
		disconnectCountEnd++;
	}

	public void incrementIncomingMessageEndCount() {
		incomingMessageCountEnd++;
	}

	public void incrementProfileRequestEndCount() {
		profileRequestCountEnd++;
	}
	
	public void incrementListCannedMsgEndCount() {
		listCannedMsgCountEnd++;
	}
	
	public void incrementListCannedMsgStartCount() {
		listCannedMsgCountStart++;
	}
	
	public void incrementGetParamStartCount() {
		getParamCountStart++;
	}
	
	public void incrementGetParamEndCount() {
		getParamCountEnd++;
	}
	
	public void incrementGetGroupInfoStartCount() {
		getGroupInfoCountStart++;
	}
	
	public void incrementGetGroupInfoEndCount() {
		getGroupInfoCountEnd++;
	}
	
	public void incrementGetSecurityQuestionsStartCount() {
		getSecurityQuestionsCountStart++;
	}
	
	public void incrementGetSecurityQuestionsEndCount() {
		getSecurityQuestionsCountEnd++;
	}
	
	public void incrementResetPasswordStartCount() {
		resetPasswordCountStart++;
	}
	
	public void incrementResetPasswordEndCount() {
		resetPasswordCountEnd++;
	}
	
	public void incrementRecoverUsernameStartCount() {
		recoverLostUsernameCountStart++;
	}
	
	public void incrementRecoverUsernameEndCount() {
		recoverLostUsernameCountEnd++;
	}
	
	public void reset() {
		connectCountStart = connectCountEnd = 
			disconnectCountStart = disconnectCountEnd = 
				incomingMessageCountStart = incomingMessageCountEnd = 
					profileRequestCountStart = profileRequestCountEnd = 
						 listCannedMsgCountStart =  listCannedMsgCountEnd = 
							 getParamCountStart = getParamCountEnd = 
								 getGroupInfoCountStart = getGroupInfoCountEnd = 
									 getSecurityQuestionsCountStart = getSecurityQuestionsCountEnd = 
										 resetPasswordCountStart = resetPasswordCountEnd = 
											 recoverLostUsernameCountStart = recoverLostUsernameCountEnd = 0;
	}
}
