package name.bychkov.junit5.params;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SubClassProducerTest
{
	@Test
	void testDefineMethodExists()
	{
		try
		{
			ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
		}
		catch (NoSuchMethodException | SecurityException e)
		{
			Assertions.fail("Method java.lang.ClassLoader#defineClass(String, byte[], int, int) is unaccessible", e);
		}
	}
}