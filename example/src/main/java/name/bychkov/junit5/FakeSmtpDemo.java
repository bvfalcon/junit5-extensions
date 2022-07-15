package name.bychkov.junit5;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class FakeSmtpDemo
{
	static final int smtpPort = 2567;
	
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