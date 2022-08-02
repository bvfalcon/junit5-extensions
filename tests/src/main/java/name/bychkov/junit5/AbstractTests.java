package name.bychkov.junit5;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.StringUtils;
import org.opentest4j.AssertionFailedError;

abstract class AbstractTests
{
	private static final Logger LOG = LoggerFactory.getLogger(AbstractTests.class);
	
	@SuppressWarnings("unchecked")
	protected Set<Serializable> readFile()
	{
		try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(CheckAnnotationProcessor.dataFileLocation))
		{
			if (inputStream == null)
			{
				return Collections.emptySet();
			}
			final int bufLen = 4 * 0x400; // 4KB
			byte[] buf = new byte[bufLen];
			int readLen;
			try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream())
			{
				while ((readLen = inputStream.read(buf, 0, bufLen)) != -1)
				{
					outputStream.write(buf, 0, readLen);
				}
				byte[] bytes = outputStream.toByteArray();
				ObjectInput in = new ObjectInputStream(new GZIPInputStream(new ByteArrayInputStream(bytes)));
				return (Set<Serializable>) in.readObject();
			}
		}
		catch (Throwable e)
		{
			LOG.info(e, () -> String.format("Error has acquired while file reading: %s", e.getMessage()));
			return Collections.emptySet();
		}
	}
	
	protected static AssertionFailedError createAssertionFailedError(String message, Throwable exception, String errorMessageFormat, Object... args)
	{
		if (exception instanceof AssertionFailedError)
		{
			return (AssertionFailedError) exception;
		}
		if (StringUtils.isNotBlank(message))
		{
			throw new AssertionFailedError(message);
		}
		else
		{
			if (exception != null)
			{
				throw new AssertionFailedError(String.format(errorMessageFormat, args), exception);
			}
			else
			{
				throw new AssertionFailedError(String.format(errorMessageFormat, args));
			}
		}
	}
}