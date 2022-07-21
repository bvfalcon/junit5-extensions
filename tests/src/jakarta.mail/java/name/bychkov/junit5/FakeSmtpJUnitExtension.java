package name.bychkov.junit5;

import java.util.ArrayList;
import java.util.List;

import org.subethamail.wiser.WiserMessage;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

public class FakeSmtpJUnitExtension extends AbstractFakeSmtpJUnitExtension
{
	public FakeSmtpJUnitExtension port(int port)
	{
		super.port(port);
		return this;
	}
	
	public List<MimeMessage> getMessages() throws MessagingException
	{
		List<MimeMessage> messages = new ArrayList<>(server.getMessages().size());
		for (WiserMessage wMessage : server.getMessages())
		{
			messages.add(wMessage.getMimeMessage());
		}
		return messages;
	}
}