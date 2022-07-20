package name.bychkov.junit5;

import java.util.Properties;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

public class FakeSmtpJakartaMailDemo
{
	static final int smtpPort = 2568;
	
	void sendMessage(String email, String subject, String body) throws MessagingException
	{
		Properties props = System.getProperties();
		props.put("mail.smtp.host", "localhost");
		props.put("mail.smtp.port", smtpPort);
		Session session = Session.getInstance(props, null);
		
		Message simpleMail = new MimeMessage(session);

		simpleMail.setSubject(subject);
		simpleMail.setRecipient(Message.RecipientType.TO, new InternetAddress(email));

		MimeMultipart mailContent = new MimeMultipart();

		MimeBodyPart mailMessage = new MimeBodyPart();
		mailMessage.setContent(body, "text/html; charset=utf-8");
		mailContent.addBodyPart(mailMessage);

		simpleMail.setContent(mailContent);

		Transport.send(simpleMail);
	}
}