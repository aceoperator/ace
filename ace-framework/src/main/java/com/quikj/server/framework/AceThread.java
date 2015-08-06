package com.quikj.server.framework;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.WeakHashMap;

public class AceThread extends Thread {
	private LinkedList messageQueue = null;

	private Object errorLock = new Object();

	private String errorMessage = "";

	private AceOperationContextInterface operationContext = null;

	private int aceThreadListId;

	// The remLock takes care of a rare situation when a different thread is
	// removing an element from
	// the queue while this thread is waiting for a message. If this lock is not
	// used, there could
	// be a race condition causing errors.
	private Object remLock = new Object();

	private static WeakHashMap aceThreadList = new WeakHashMap();

	private static Random randomNumberGenerator = new Random(
			(new Date()).getTime());

	public AceThread() {
		super();
		createMessageQueue();
		aceThreadListId = getUniqueAceThreadId();
		synchronized (aceThreadList) {
			aceThreadList.put(new Integer(aceThreadListId), this);
		}
	}

	public AceThread(boolean nomsgq) {
		super();

		if (nomsgq == false) {
			createMessageQueue();
		}

		aceThreadListId = getUniqueAceThreadId();
		synchronized (aceThreadList) {
			aceThreadList.put(new Integer(aceThreadListId), this);
		}
	}

	public AceThread(String name) {
		super(name);
		createMessageQueue();
		aceThreadListId = getUniqueAceThreadId();
		synchronized (aceThreadList) {
			aceThreadList.put(new Integer(aceThreadListId), this);
		}
	}

	public AceThread(String name, boolean nomsgq) {
		super(name);

		if (nomsgq == false) {
			createMessageQueue();
		}

		aceThreadListId = getUniqueAceThreadId();
		synchronized (aceThreadList) {
			aceThreadList.put(new Integer(aceThreadListId), this);
		}
	}

	public AceThread(ThreadGroup group, String name) {
		super(group, name);
		createMessageQueue();
		aceThreadListId = getUniqueAceThreadId();
		synchronized (aceThreadList) {
			aceThreadList.put(new Integer(aceThreadListId), this);
		}
	}

	public AceThread(ThreadGroup group, String name, boolean nomsgq) {
		super(group, name);

		if (nomsgq == false) {
			createMessageQueue();
		}

		aceThreadListId = getUniqueAceThreadId();
		synchronized (aceThreadList) {
			aceThreadList.put(new Integer(aceThreadListId), this);
		}
	}

	public static AceThread getAceThreadObject(int code) {
		synchronized (aceThreadList) {
			return (AceThread) aceThreadList.get(new Integer(code));
		}
	}

	public static int getUniqueAceThreadId() {
		do {
			int random_num = randomNumberGenerator.nextInt();
			if (random_num <= 0)
				continue;

			synchronized (aceThreadList) {
				if (aceThreadList.get(new Integer(random_num)) == null) { 
					// not found
					return random_num;
				}
			}
		} while (true);
	}

	private void createMessageQueue() {
		messageQueue = new LinkedList();
	}

	protected void dispatchErrorMessage(String error, Throwable e) {
		synchronized (errorLock) {
			if (e != null) {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				PrintStream stream = new PrintStream(bos);
				e.printStackTrace(stream);
				errorMessage = error + "\nException trace:\n" + bos.toString();
				stream.close();
				try {
					bos.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			} else {
				errorMessage = error;
			}
		}
	}

	public void dispose() {
		if (messageQueue != null) {
			messageQueue.clear();
			messageQueue = null;
		}

		synchronized (aceThreadList) {
			aceThreadList.remove(new Integer(aceThreadListId));
		}

	}

	public final boolean flushMessages() {
		if (messageQueue == null) {
			writeErrorMessage("The thread has not enabled message queue", null);
			return false;
		}

		synchronized (remLock) {
			synchronized (messageQueue) {
				messageQueue.clear();
			}
		}
		return true;
	}

	protected final int getAceThreadId() {
		return aceThreadListId;
	}

	public final String getErrorMessage() {
		synchronized (errorLock) {
			String error = new String(errorMessage);
			errorMessage = "";
			return error;
		}
	}

	protected final AceOperationContextInterface getOperationContext() {
		return operationContext;
	}

	public final boolean interruptWait(int sig_id) {
		return interruptWait(sig_id, "");
	}

	public final boolean interruptWait(int sig_id, String message) {
		// send a signal message
		return sendMessage(new AceSignalMessage(sig_id, message));
	}

	public final boolean removeMessage(AceMessageInterface obj,
			AceCompareMessageInterface comp) {
		boolean removed = false;

		if (messageQueue == null) {
			writeErrorMessage("The thread has not enabled message queue", null);
			return false;
		}

		synchronized (remLock) {
			synchronized (messageQueue) {
				ListIterator iter = messageQueue.listIterator(0);

				while (iter.hasNext() == true) {
					AceMessageInterface msg = (AceMessageInterface) iter.next();
					if (comp.same(obj, msg) == true) // found
					{
						// remove the message from the queue
						iter.remove();
						removed = true;
					}
				}
			}
		}

		return removed;
	}

	public final boolean sendMessage(AceMessageInterface msg) {
		if (messageQueue == null) {
			writeErrorMessage("The thread has not enabled message queue", null);
			return false;
		}

		synchronized (messageQueue) { // lock access
			messageQueue.addLast(msg);
			messageQueue.notify();
		}

		return true;
	}

	public final boolean sendHighPriortyMessage(AceMessageInterface msg) {
		if (messageQueue == null) {
			writeErrorMessage("The thread has not enabled message queue", null);
			return false;
		}

		synchronized (messageQueue) { // lock access
			messageQueue.addFirst(msg);
			messageQueue.notify();
		}

		return true;
	}

	protected final void setOperationContext(
			AceOperationContextInterface context) {
		operationContext = context;
	}

	protected final AceMessageInterface waitMessage() {
		if (messageQueue == null) {
			writeErrorMessage("The thread has not enabled message queue", null);
			return null;
		}

		synchronized (remLock) {
			synchronized (messageQueue) {
				// there is something in the queue
				if (messageQueue.size() > 0) {
					try {
						return (AceMessageInterface) messageQueue.removeFirst();
					} catch (NoSuchElementException ex1) {
						writeErrorMessage("Element not found in queue", null);
						return null;
					}
				} else { // nothing in the message queue
					while (true) {
						try {
							messageQueue.wait();
							break;
						} catch (InterruptedException ex) {
							// ignore for the time-being
						}
					}

					// A message has arrived
					try {
						return (AceMessageInterface) messageQueue.removeFirst();
					} catch (NoSuchElementException ex1) {
						writeErrorMessage("Element not found in queue", null);
						return null;
					}
				}
			}
		}
	}

	protected void writeErrorMessage(String error, Throwable e) {
		Thread cthread = Thread.currentThread();

		if ((cthread instanceof AceThread) == true) {
			((AceThread) cthread).dispatchErrorMessage(error, e);
		} else {
			System.err.println(error);
		}
	}
}
