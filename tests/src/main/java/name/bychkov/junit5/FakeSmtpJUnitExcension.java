package name.bychkov.junit5;

import java.net.InetAddress;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import com.nilhcem.fakesmtp.FakeSMTP;

public class FakeSmtpJUnitExcension implements BeforeAllCallback, AfterAllCallback, AfterEachCallback
{
	private InetAddress host;
	private int port = 25;
	
	public FakeSmtpJUnitExcension host(InetAddress host)
	{
		this.host = host;
		return this;
	}

	public FakeSmtpJUnitExcension port(int port)
	{
		this.port = port;
		return this;
	}
	
	@Override
	public void afterEach(ExtensionContext context) throws Exception
	{
		FakeSMTP.deleteEmails();
	}

	@Override
	public void afterAll(ExtensionContext context) throws Exception
	{
		FakeSMTP.down();
	}

	@Override
	public void beforeAll(ExtensionContext context) throws Exception
	{
		InetAddress hostLocal = host != null ? host : InetAddress.getByName("localhost");
		FakeSMTP.up(port, hostLocal);
	}
}