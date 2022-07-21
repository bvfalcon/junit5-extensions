package name.bychkov.junit5;

import java.io.IOException;
import java.util.Random;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

public class FakeSmtpJakartaMailDemoTest
{
	@RegisterExtension
	static FakeSmtpJUnitExtension fakeSmtp = new FakeSmtpJUnitExtension().port(FakeSmtpJakartaMailDemo.smtpPort);

	@Test
	public void testSendMessage()
	{
		String receiver = "test-email-"+ new Random().nextInt(Integer.MAX_VALUE)+"@example.com";
		String subject = "test-subject-"+ new Random().nextInt(Integer.MAX_VALUE);
		String body = "test-body-"+ new Random().nextInt(Integer.MAX_VALUE);
		FakeSmtpJakartaMailDemo testedObject = new FakeSmtpJakartaMailDemo();
		try
		{
			testedObject.sendMessage(receiver, subject, body);
			Assertions.assertEquals(1, fakeSmtp.getMessages().size());
			MimeMessage actualMail = fakeSmtp.getMessages().iterator().next();
			Assertions.assertEquals(subject, actualMail.getSubject());
			Assertions.assertEquals(receiver, actualMail.getAllRecipients()[0].toString());
			Assertions.assertEquals(body, actualMail.getContent().toString().trim());
		}
		catch (MessagingException | IOException e)
		{
			Assertions.fail(e);
		}
	}
}