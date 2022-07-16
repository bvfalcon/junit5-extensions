package name.bychkov.junit5;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

public class FakeSmtpJUnitExcension implements BeforeAllCallback, AfterAllCallback, AfterEachCallback
{
	private static Wiser server;
	private int port = 25;
	
	public FakeSmtpJUnitExcension port(int port)
	{
		this.port = port;
		return this;
	}
	
	@Override
	public void afterEach(ExtensionContext context) throws Exception
	{
		server.getMessages().clear();
	}

	@Override
	public void afterAll(ExtensionContext context) throws Exception
	{
		server.stop();
	}

	@Override
	public void beforeAll(ExtensionContext context) throws Exception
	{
		server = Wiser.port(port);
		server.start();
	}
	
	public List<EMailMessage> getMessages()
	{
		return server.getMessages().stream().map(EMailMessage::new).collect(Collectors.toList());
	}
}