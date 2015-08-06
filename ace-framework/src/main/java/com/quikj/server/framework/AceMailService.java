/*
 * AceMailService.java
 *
 * Created on November 16, 2002, 7:30 PM
 */

package com.quikj.server.framework;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import javax.mail.AuthenticationFailedException;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;

/**
 * 
 * @author bhm
 */
public class AceMailService extends AceThread {

	private class SmtpAuthenticator extends Authenticator {

		private String authName;
		private String authPass;

		public SmtpAuthenticator(String name, String password) {
			authName = name;
			authPass = password;
		}

		protected PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(authName, authPass);
		}
	}

	private static AceMailService instance = null;

	private Session mailSession;

	private ArrayList<AceMailMessage> pendingMessageList = new ArrayList<AceMailMessage>();
	private int retryTimerId = -1;
	private static final long RETRY_SENDING = 0;

	private static final int RETRY_INTERVAL = 30 * 1000;

	private Date accLogged = new Date();

	private String pendingFile;

	private String overrideFrom;

	private String pendingDir;

	public AceMailService(String server, int port, boolean tls, boolean debug,
			String username, String password, String pendingDir,
			String pendingFile, String overrideFrom) {
		super("AceMailService");

		this.pendingDir = AceConfigFileHelper.getAcePath(pendingDir);
		this.pendingFile = pendingFile;
		this.overrideFrom = overrideFrom;

		initMail(server, port, tls, debug, username, password);
		instance = this;
	}

	private void initMail(String server, int port, boolean tls, boolean debug,
			String username, String password) {

		// do mail server setup
		Properties props = new Properties();

		// TODO - fix this
		// props.put("mail.smtp.user", d_email);

		props.setProperty("mail.smtp.host", server);
		props.setProperty("mail.smtp.port", Integer.toString(port));

		if (tls) {
			props.setProperty("mail.smtp.starttls.enable", "true");
			props.setProperty("mail.smtp.socketFactory.class",
					"javax.net.ssl.SSLSocketFactory");
			props.setProperty("mail.smtp.socketFactory.fallback", "false");
			props.setProperty("mail.smtp.socketFactory.port",
					Integer.toString(port));
		}

		if (debug) {
			props.setProperty("mail.smtp.debug", "true");
		}

		SmtpAuthenticator authenticator = null;
		if (username != null && username.length() > 0) {
			props.setProperty("mail.smtp.auth", "true");
			authenticator = new SmtpAuthenticator(username, password);
		}

		mailSession = Session.getDefaultInstance(props, authenticator);
		mailSession.setDebug(debug);
	}

	public static AceMailService getInstance() {
		return instance;
	}

	public boolean addToMailQueue(AceMailMessage msg) {
		return sendMessage(new AceMailServiceMessage(msg));
	}

	private void cleanup() {
		if (retryTimerId != -1) {
			AceTimer.Instance().cancelTimer(retryTimerId);
			retryTimerId = -1;
		}

		// serialize out the pending message list
		if (pendingMessageList.size() > 0) {
			File file = new File(pendingFile);

			try {
				ObjectOutputStream os = new ObjectOutputStream(
						new FileOutputStream(file));
				os.writeObject(pendingMessageList);
				os.close();
			} catch (Exception ex) {
				AceLogger.Instance().log(
						AceLogger.ERROR,
						AceLogger.SYSTEM_LOG,
						getName() + "- dispose() -- Error while serializing - "
								+ ex.getClass().getName() + " : "
								+ ex.getMessage());
			}
		}

		instance = null;
		super.dispose();

	}

	public void dispose() {
		// interrupt the wait (kill this thread)
		interruptWait(AceSignalMessage.SIGNAL_TERM, "disposed");

		instance = null;
	}

	public Session getMailSession() {
		return mailSession;
	}

	private boolean handleMail(AceMailMessage mail_message) {
		// override from/reply-to if specified in the config file
		if (overrideFrom != null && overrideFrom.length() > 0) {
			mail_message.setFrom(overrideFrom);
		}

		javax.mail.Message message = mail_message.toEmail(mailSession);
		if (message == null) {
			// log message
			AceLogger.Instance().log(
					AceLogger.WARNING,
					AceLogger.SYSTEM_LOG,
					"AceMailService.handleMail() -- Outgoing email message discarded : "
							+ mail_message.getErrorMessage());

			return true;
		}

		try {
			// send the message

			Transport tr = mailSession.getTransport("smtp");
			tr.connect();
			tr.sendMessage(message, message.getAllRecipients());
			tr.close();

		} catch (SendFailedException ex) {
			// log a warning
			return false;
		} catch (AuthenticationFailedException ex) {
			// log a warning
			return false;
		} catch (Exception ex) {
			// don't retry on remaining/other exceptions
			// FolderClosedException, FolderNotFoundException,
			// IllegalWriteException, MessageRemovedException,
			// MethodNotSupportedException,
			// NoSuchProviderException, ParseException, ReadOnlyFolderException,
			// SearchException,
			// StoreClosedException

			// log a warning
			return true;
		} finally {
			// check the accumulated message queue
			int size = pendingMessageList.size();
			if (size > 0) {
				Date current_time = new Date();
				if (current_time.getTime() >= (accLogged.getTime() + 30 * 60 * 1000)) {
					// print log message
					AceLogger.Instance().log(
							AceLogger.WARNING,
							AceLogger.SYSTEM_LOG,
							"AceMailService.handleMail() -- Accumulated mail queue size is  "
									+ size);
					accLogged = current_time;
				}
			}
		}
		return true;
	}

	public void run() {
		AceLogger.Instance().log(AceLogger.INFORMATIONAL, AceLogger.SYSTEM_LOG,
				"AceMailService.run() -- Ace mail service started");

		// serialize in the pending message file, remove the file, process the
		// pending messages

		File file = new File(pendingDir, pendingFile);

		if (file.exists()) {
			try {
				ObjectInputStream is = new ObjectInputStream(
						new FileInputStream(file));
				pendingMessageList = (ArrayList<AceMailMessage>) is
						.readObject();
				is.close();

				if (file.delete() == false) {
					AceLogger
							.Instance()
							.log(AceLogger.ERROR,
									AceLogger.SYSTEM_LOG,
									getName()
											+ "- run() -- Failure deleting save pending mail file "
											+ file.getAbsolutePath()
											+ ". Please remove the file manually.");
				}

				sendPendingMessage();
			} catch (Exception ex) {
				AceLogger
						.Instance()
						.log(AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								getName()
										+ "- run() -- Error while serializing in saved pending mail file, pending messages discarded. Error = "
										+ ex.getClass().getName() + " : "
										+ ex.getMessage());

				if (file.delete() == false) {
					AceLogger
							.Instance()
							.log(AceLogger.ERROR,
									AceLogger.SYSTEM_LOG,
									getName()
											+ "- run() -- Failure deleting save pending mail file "
											+ file.getAbsolutePath()
											+ ". Please remove the file manually.");
				}
			}
		}

		while (true) {
			AceMessageInterface message = waitMessage();
			if (message == null) {
				// print error message
				AceLogger
						.Instance()
						.log(AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								getName()
										+ "- run() -- A null message was received while waiting for a message - "
										+ getErrorMessage());

				break;
			}

			if (message instanceof AceSignalMessage) {
				// A signal message is received

				// print informational message
				AceLogger.Instance().log(
						AceLogger.INFORMATIONAL,
						AceLogger.SYSTEM_LOG,
						getName() + " - AceMailService.run() --  A signal "
								+ ((AceSignalMessage) message).getSignalId()
								+ " is received : "
								+ ((AceSignalMessage) message).getMessage());

				break;
			} else if ((message instanceof AceMailServiceMessage) == true) {
				AceMailMessage mail_message = ((AceMailServiceMessage) message)
						.getMailMessage();

				// if we have a backlog of outgoing msgs, put message in pending
				// queue
				if (retryTimerId != -1) {
					pendingMessageList.add(mail_message);
				} else {
					// send the message

					if (handleMail(mail_message) == false) {
						// put the message in the pending queue and start retry
						// timing
						pendingMessageList.add(mail_message);

						retryTimerId = AceTimer.Instance().startTimer(
								RETRY_INTERVAL, RETRY_SENDING);
					}
				}
			} else if (message instanceof AceTimerMessage) {
				AceTimerMessage timer_msg = (AceTimerMessage) message;

				if (timer_msg.getUserSpecifiedParm() == RETRY_SENDING) {
					sendPendingMessage();
				} else {
					// print error message
					AceLogger
							.Instance()
							.log(AceLogger.ERROR,
									AceLogger.SYSTEM_LOG,
									getName()
											+ "- run() -- An unexpected timeout is received : "
											+ timer_msg.getUserSpecifiedParm());
				}
			} else {
				// unexpected event
				// print error message
				AceLogger
						.Instance()
						.log(AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								getName()
										+ "- run() -- An unexpected message is received : "
										+ message.messageType());
			}
		}

		cleanup();
	}

	private void sendPendingMessage() {
		if (pendingMessageList.size() > 0) {
			AceMailMessage msg = pendingMessageList.get(0);
			if (handleMail(msg) == true) {
				pendingMessageList.remove(0);
			}

			if (pendingMessageList.size() > 0) {
				retryTimerId = AceTimer.Instance().startTimer(RETRY_INTERVAL,
						RETRY_SENDING);
				return;
			}
		}

		retryTimerId = -1;
	}

}
