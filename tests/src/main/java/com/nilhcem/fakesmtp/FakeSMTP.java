package com.nilhcem.fakesmtp;

import java.net.InetAddress;
import java.util.Collection;

import com.nilhcem.fakesmtp.core.exception.BindPortException;
import com.nilhcem.fakesmtp.core.exception.OutOfRangePortException;
import com.nilhcem.fakesmtp.model.EmailModel;
import com.nilhcem.fakesmtp.server.SMTPServerHandler;

/**
 * Entry point of the application.
 *
 * @author Nilhcem
 * @since 1.0
 */
public final class FakeSMTP {

	private FakeSMTP() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Useful for usage in JUnit-tests
	 * */
	public static void up(int port, InetAddress bindAddress) throws BindPortException, OutOfRangePortException {
		if (!SMTPServerHandler.INSTANCE.isRunning()) {
			SMTPServerHandler.INSTANCE.startServer(port, bindAddress);
		}
	}
	
	/**
	 * Useful for usage in JUnit-tests
	 * */
	public static void down() {
		SMTPServerHandler.INSTANCE.stopServer();
	}
	
	/**
	 * Useful for usage in JUnit-tests
	 * */
	public static Collection<EmailModel> getEmails() {
		return SMTPServerHandler.INSTANCE.getMailSaver().getEmails();
	}
	
	/**
	 * Useful for usage in JUnit-tests
	 * */
	public static void deleteEmails() {
		SMTPServerHandler.INSTANCE.getMailSaver().deleteEmails();
	}
}