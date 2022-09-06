package name.bychkov.junit5;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;

import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.StringUtils;
import org.opentest4j.AssertionFailedError;

public abstract class AbstractTests
{
	private static final Logger LOG = LoggerFactory.getLogger(AbstractTests.class);
	
	@SuppressWarnings("unchecked")
	protected Set<Serializable> readFile(String filename)
	{
		URL resource = getClass().getClassLoader().getResource(filename);
		if (resource == null)
		{
			return Collections.emptySet();
		}
		try (InputStream inputStream = resource.openStream())
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
				ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(bytes));
				return (Set<Serializable>) in.readObject();
			}
		}
		catch (Throwable e)
		{
			LOG.info(e, () -> String.format("Error has acquired while file reading: %s", e.getMessage()));
			return Collections.emptySet();
		}
		finally
		{
			try
			{
				Path path = Paths.get(resource.toURI());
				Files.deleteIfExists(path);
				Files.deleteIfExists(path.getParent());
				Files.deleteIfExists(path.getParent().getParent());
			}
			catch (DirectoryNotEmptyException e)
			{
				LOG.debug(e, () -> String.format("Error has acquired while directory removing: %s", e.getMessage()));
			}
			catch (IOException | URISyntaxException e)
			{
				LOG.warn(e, () -> String.format("Error has acquired while file or directory removing: %s", e.getMessage()));
			}
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