package com.nilhcem.fakesmtp.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nilhcem.fakesmtp.model.EmailModel;

/**
 * Saves emails and notifies components so they can refresh their views with new data.
 *
 * @author Nilhcem
 * @since 1.0
 */
public final class MailSaver extends Observable {

	private static final Logger LOGGER = LoggerFactory.getLogger(MailSaver.class);
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");
	// This can be a static variable since it is Thread Safe
	private static final Pattern SUBJECT_PATTERN = Pattern.compile("^Subject: (.*)$");

	private final SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyhhmmssSSS");
	private List<EmailModel> emails = new ArrayList<>();

	/**
	 * Saves incoming email in file system and notifies observers.
	 *
	 * @param from the user who send the email.
	 * @param to the recipient of the email.
	 * @param data an InputStream object containing the email.
	 * @see com.nilhcem.fakesmtp.gui.MainPanel#addObservers to see which observers will be notified
	 */
	public void saveEmailAndNotify(String from, String to, InputStream data) {
		// We move everything that we can move outside the synchronized block to limit the impact
		EmailModel model = new EmailModel();
		model.setFrom(from);
		model.setTo(to);
		String mailContent = convertStreamToString(data);
		model.setSubject(getSubjectFromStr(mailContent));
		model.setEmailStr(mailContent);
		model.setReceivedDate(new Date());
		
		emails.add(model);
	}
	
	public Collection<EmailModel> getEmails() {
		return emails;
	}

	/**
	 * Deletes all received emails
	 */
	public void deleteEmails() {
		emails.clear();
	}

	/**
	 * Converts an {@code InputStream} into a {@code String} object.
	 * <p>
	 * The method will not copy the first 4 lines of the input stream.<br>
	 * These 4 lines are SubEtha SMTP additional information.
	 * </p>
	 *
	 * @param is the InputStream to be converted.
	 * @return the converted string object, containing data from the InputStream passed in parameters.
	 */
	private String convertStreamToString(InputStream is) {
		final long lineNbToStartCopy = 4; // Do not copy the first 4 lines (received part)
		BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
		StringBuilder sb = new StringBuilder();

		String line;
		long lineNb = 0;
		try {
			while ((line = reader.readLine()) != null) {
				if (++lineNb > lineNbToStartCopy) {
					sb.append(line).append(LINE_SEPARATOR);
				}
			}
		} catch (IOException e) {
			LOGGER.error("", e);
		}
		return sb.toString();
	}

	/**
	 * Gets the subject from the email data passed in parameters.
	 *
	 * @param data a string representing the email content.
	 * @return the subject of the email, or an empty subject if not found.
	 */
	private String getSubjectFromStr(String data) {
		try {
			BufferedReader reader = new BufferedReader(new StringReader(data));

			String line;
			while ((line = reader.readLine()) != null) {
				 Matcher matcher = SUBJECT_PATTERN.matcher(line);
				 if (matcher.matches()) {
					 return matcher.group(1);
				 }
			}
		} catch (IOException e) {
			LOGGER.error("", e);
		}
		return "";
	}
}
