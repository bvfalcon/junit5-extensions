package name.bychkov.junit5;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.subethamail.wiser.WiserMessage;

public class EMailMessage
{
	private String sender;
	private String receiver;
	private String subject;
	private String body;
	
	private static final Pattern SUBJECT_PATTERN = Pattern.compile("^Subject: (.*)$");
	
	public EMailMessage(WiserMessage message)
	{
		this.sender = message.getEnvelopeSender();
		this.receiver = message.getEnvelopeReceiver();
		String messageText = message.toString();
		this.subject = getSubjectFromStr(messageText);
	}
	
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
		}
		return "";
	}

	public String getSender() {
		return sender;
	}

	public String getReceiver() {
		return receiver;
	}

	public String getSubject() {
		return subject;
	}

	public String getBody() {
		return body;
	}
}
