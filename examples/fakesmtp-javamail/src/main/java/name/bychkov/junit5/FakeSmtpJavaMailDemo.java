package name.bychkov.junit5;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class FakeSmtpJavaMailDemo
{
	static final int smtpPort = 2569;
	
	void sendMessage(String email, String subject, String body) throws MessagingException
	{
		Properties props = System.getProperties();
		props.put("mail.smtp.host", "localhost");
		props.put("mail.smtp.port", smtpPort);
		Session session = Session.getInstance(props, null);
		
		MimeMessage simpleMail = new MimeMessage(session);
		
		simpleMail.setSubject(subject);
		simpleMail.setRecipient(Message.RecipientType.TO, new InternetAddress(email));
		simpleMail.setText(body, StandardCharsets.UTF_8.toString());
		
		Transport.send(simpleMail);
	}
}