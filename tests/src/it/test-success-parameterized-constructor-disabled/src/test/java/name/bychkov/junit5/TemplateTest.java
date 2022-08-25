package name.bychkov.junit5;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import name.bychkov.junit5.params.ParameterizedConstructor;
import name.bychkov.junit5.params.provider.ValueSource;

@Disabled
public abstract class TemplateTest
{
	@ParameterizedConstructor
	@ValueSource(strings = { "test-value-1", "test-value-2" })
	public TemplateTest(String constructorParameter)
	{
	}
	
	@Test
	public void test1()
	{
		Assertions.fail("This test must be not executed");
	}
}