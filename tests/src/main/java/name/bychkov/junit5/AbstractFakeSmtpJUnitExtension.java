package name.bychkov.junit5;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.subethamail.wiser.Wiser;

abstract class AbstractFakeSmtpJUnitExtension implements BeforeAllCallback, AfterAllCallback, AfterEachCallback
{
	protected static Wiser server;
	private int port = 25;
	
	protected AbstractFakeSmtpJUnitExtension port(int port)
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
}