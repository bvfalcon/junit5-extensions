package name.bychkov.junit5;

import java.util.Random;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.mail.MessagingException;

public class FakeSmtpJavaMailDemoTest
{
	@RegisterExtension
	static FakeSmtpJUnitExcension fakeSmtp = new FakeSmtpJUnitExcension().port(FakeSmtpJavaMailDemo.smtpPort);

	@Test
	public void testSendMessage()
	{
		String receiver = "test-email-"+ new Random().nextInt(Integer.MAX_VALUE)+"@example.com";
		String subject = "test-subject-"+ new Random().nextInt(Integer.MAX_VALUE);
		String body = "test-body-"+ new Random().nextInt(Integer.MAX_VALUE);
		FakeSmtpJavaMailDemo testedObject = new FakeSmtpJavaMailDemo();
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