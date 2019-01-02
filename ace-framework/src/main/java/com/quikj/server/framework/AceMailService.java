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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

/**
 * 
 * @author bhm
 */
public class AceMailService extends AceThread {

	private static AceMailService instance = null;

	private ArrayList<AceMailMessage> pendingMessageList = new ArrayList<AceMailMessage>();
	private int retryTimerId = -1;
	private static final long RETRY_SENDING = 0;

	private static final int RETRY_INTERVAL = 30 * 1000;

	private Date accLogged = new Date();

	private String pendingFile;

	private String overrideFrom;

	private String pendingDir;

	private JavaMailSenderImpl mailSender;

	public static AceMailService getInstance() {
		return instance;
	}

	public AceMailService(String server, int port, boolean tls, boolean debug, String username, String password,
			String pendingDir, String pendingFile, String overrideFrom) {
		super("AceMailService");

		this.pendingDir = AceConfigFileHelper.getAcePath(pendingDir);
		this.pendingFile = pendingFile;
		this.overrideFrom = overrideFrom;

		initMail(server, port, tls, debug, username, password);
		instance = this;
	}

	private void initMail(String server, int port, boolean tls, boolean debug, String username, String password) {
		mailSender = new JavaMailSenderImpl();
		Properties props = mailSender.getJavaMailProperties();

		if (debug) {
			props.setProperty("mail.debug", "true");
		}

		if (tls) {
			props.put("mail.smtp.starttls.enable", "true");
		}

		props.put("mail.transport.protocol", "smtp");
		mailSender.setHost(server);
		mailSender.setPort(port);

		if (username != null && username.length() > 0) {
			props.put("mail.smtp.auth", "true");
			mailSender.setUsername(username);
			mailSender.setPassword(password);
		}
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
			AceLogger.Instance().log(AceLogger.INFORMATIONAL, AceLogger.SYSTEM_LOG, getName()
					+ "- cleanup() -- Writing " + pendingMessageList.size() + " unsent mail messages to file");
			File file = new File(pendingFile);

			try {
				ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(file));
				os.writeObject(pendingMessageList);
				os.close();
			} catch (Exception e) {
				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG, getName()
						+ "- cleanup() -- Error while serializing - " + e.getClass().getName() + " : " + e.getMessage(),
						e);
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

	private boolean addressValid(String addr) {
		String[] tokens = addr.split("@");
		if (tokens.length < 2) {
			return false;
		}

		String[] domain = tokens[1].split("\\.");
		if (domain.length < 2) {
			return false;
		}

		return true;
	}

	private void setAddressList(List<String> addresses, Consumer<String[]> function) {
		if (!addresses.isEmpty()) {
			for (String address : addresses) {
				if (!addressValid(address)) {
					throw new AceRuntimeException("Invalid address " + address);
				}
			}
			function.accept(addresses.toArray(new String[addresses.size()]));
		}
	}

	@FunctionalInterface
	public interface ThrowingConsumer<T, E extends Exception> {
		void accept(T t) throws E;
	}

	static <T> Consumer<T> throwingConsumerWrapper(ThrowingConsumer<T, Exception> throwingConsumer) {
		return i -> {
			try {
				throwingConsumer.accept(i);
			} catch (Exception ex) {
				throw new AceRuntimeException(ex);
			}
		};
	}

	private MimeMessage setupEmail(AceMailMessage mailMessage) throws MessagingException, UnsupportedEncodingException {
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true);

		helper.setFrom(mailMessage.getFrom());

		if (mailMessage.getTo().isEmpty() && mailMessage.getCc().isEmpty() && mailMessage.getBcc().isEmpty()) {
			throw new AceRuntimeException("There are no recipeints");
		}

		setAddressList(mailMessage.getReplyTo(), throwingConsumerWrapper(replyTos -> helper.setReplyTo(replyTos[0])));

		setAddressList(mailMessage.getTo(), throwingConsumerWrapper(recipients -> helper.setTo(recipients)));

		setAddressList(mailMessage.getCc(), throwingConsumerWrapper(ccs -> helper.setCc(ccs)));

		setAddressList(mailMessage.getBcc(), throwingConsumerWrapper(bccs -> helper.setBcc(bccs)));

		if (mailMessage.getSubject() != null) {

			helper.setSubject(new String(mailMessage.getSubject().getBytes("UTF-8")));
		}

		if (mailMessage.getBody() != null) {
			boolean html = mailMessage.getSubType().equalsIgnoreCase("html");
			helper.setText(new String(mailMessage.getBody().getBytes("UTF-8")), html);
		}
		return message;
	}

	private boolean handleMail(AceMailMessage mailMessage) {
		// override from/reply-to if specified in the config file
		if (overrideFrom != null && !overrideFrom.isEmpty()) {
			mailMessage.setFrom(overrideFrom);
		}

		MimeMessage message = null;
		try {
			message = setupEmail(mailMessage);
		} catch (AceRuntimeException | UnsupportedEncodingException | MessagingException e) {
			AceLogger.Instance().log(AceLogger.WARNING, AceLogger.SYSTEM_LOG,
					"AceMailService.handleMail() -- Outgoing email message discarded : " + e.getMessage(), e);
			return true;
		}

		try {
			mailSender.send(message);
		} catch (MailException ex) {
			// log a warning
			return false;
		}  catch (Exception ex) {
			// don't retry on remaining/other exceptions
			// log a warning
			return true;
		} finally {
			// check the accumulated message queue
			int size = pendingMessageList.size();
			if (size > 0) {
				Date currentTime = new Date();
				if (currentTime.getTime() >= (accLogged.getTime() + 30 * 60 * 1000)) {
					// print log message
					AceLogger.Instance().log(AceLogger.WARNING, AceLogger.SYSTEM_LOG,
							"AceMailService.handleMail() -- Accumulated mail queue size is  " + size);
					accLogged = currentTime;
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
				ObjectInputStream is = new ObjectInputStream(new FileInputStream(file));
				pendingMessageList = (ArrayList<AceMailMessage>) is.readObject();
				is.close();

				if (!file.delete()) {
					AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
							getName() + "- run() -- Failure deleting save pending mail file " + file.getAbsolutePath()
									+ ". Please remove the file manually.");
				}

				sendPendingMessage();
			} catch (Exception ex) {
				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG, getName()
						+ "- run() -- Error while serializing in saved pending mail file, pending messages discarded. Error = "
						+ ex.getClass().getName() + " : " + ex.getMessage());

				if (file.delete() == false) {
					AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
							getName() + "- run() -- Failure deleting save pending mail file " + file.getAbsolutePath()
									+ ". Please remove the file manually.");
				}
			}
		}

		while (true) {
			AceMessageInterface message = waitMessage();
			if (message == null) {
				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG, getName()
						+ "- run() -- A null message was received while waiting for a message - " + getErrorMessage());
				break;
			}

			if (message instanceof AceSignalMessage) {
				// A signal message is received
				AceLogger.Instance().log(AceLogger.INFORMATIONAL, AceLogger.SYSTEM_LOG,
						getName() + " - AceMailService.run() --  A signal " + ((AceSignalMessage) message).getSignalId()
								+ " is received : " + ((AceSignalMessage) message).getMessage());

				break;
			} else if (message instanceof AceMailServiceMessage) {
				AceMailMessage mailMessage = ((AceMailServiceMessage) message).getMailMessage();

				// if we have a backlog of outgoing msgs, put message in pending
				// queue
				if (retryTimerId != -1) {
					pendingMessageList.add(mailMessage);
				} else {
					// send the message
					if (!handleMail(mailMessage)) {
						// put the message in the pending queue and start retry
						// timing
						pendingMessageList.add(mailMessage);

						retryTimerId = AceTimer.Instance().startTimer(RETRY_INTERVAL, RETRY_SENDING);
					}
				}
			} else if (message instanceof AceTimerMessage) {
				AceTimerMessage timer_msg = (AceTimerMessage) message;

				if (timer_msg.getUserSpecifiedParm() == RETRY_SENDING) {
					sendPendingMessage();
				} else {
					AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG, getName()
							+ "- run() -- An unexpected timeout is received : " + timer_msg.getUserSpecifiedParm());
				}
			} else {
				// unexpected event
				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
						getName() + "- run() -- An unexpected message is received : " + message.messageType());
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
				retryTimerId = AceTimer.Instance().startTimer(RETRY_INTERVAL, RETRY_SENDING);
				return;
			}
		}

		retryTimerId = -1;
	}

}
