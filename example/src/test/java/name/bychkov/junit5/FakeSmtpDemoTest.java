package name.bychkov.junit5;

import java.util.Random;

import jakarta.mail.MessagingException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.subethamail.wiser.WiserMessage;

public class FakeSmtpDemoTest
{
	@RegisterExtension
	static FakeSmtpJUnitExcension fakeSmtp = new FakeSmtpJUnitExcension().port(FakeSmtpDemo.smtpPort);

	@Test
	public void testSendMessage()
	{
		String receiver = "test-email-"+ new Random().nextInt(Integer.MAX_VALUE)+"@example.com";
		String subject = "test-subject-"+ new Random().nextInt(Integer.MAX_VALUE);
		String body = "test-body-"+ new Random().nextInt(Integer.MAX_VALUE);
		FakeSmtpDemo testedObject = new FakeSmtpDemo();
		try
		{
			testedObject.sendMessage(receiver, subject, body);
			Assertions.assertEquals(1, fakeSmtp.getMessages().size());
			EMailMessage actualMail = fakeSmtp.getMessages().iterator().next();
			Assertions.assertEquals(subject, actualMail.getSubject());
			Assertions.assertEquals(receiver, actualMail.getReceiver());
		}
		catch (MessagingException e)
		{
			Assertions.fail(e);
		}
	}
}