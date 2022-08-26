package name.bychkov.junit5;

import java.util.Arrays;
import java.util.Collection;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import name.bychkov.junit5.params.ParameterizedConstructor;
import name.bychkov.junit5.params.provider.EmptySource;
import name.bychkov.junit5.params.provider.EnumSource;
import name.bychkov.junit5.params.provider.MethodSource;
import name.bychkov.junit5.params.provider.NullSource;
import name.bychkov.junit5.params.provider.ValueSource;

public abstract class TemplateTest
{
	private String constructorParameter;
	
	@ParameterizedConstructor
	@ValueSource(strings = { "test-value-1", "test-value-2" })
	//@NullSource
	//@EmptySource
	@MethodSource(value = { "testData", "name.bychkov.junit5.TemplateTest#testData2()" })
	public TemplateTest(String constructorParameter)
	{
		this.constructorParameter = constructorParameter;
	}
	
	@ParameterizedConstructor
	@EnumSource(names = { "TEST1", "TEST2" })
	public TemplateTest(TestEnum enumValue)
	{
		this.constructorParameter = enumValue.getTestValue();
	}
	
	public static Collection<String> testData()
	{
		return Arrays.asList("test-value-3", "test-value-4");
	}
	
	public static Collection<String> testData2()
	{
		return Arrays.asList("test-value-5", "test-value-6");
	}
	
	@BeforeEach
	public void startUp()
	{
		// preparations for each test
	}
	
	@AfterEach
	public void tearDown()
	{
		// cleanup after each test
	}
	
	@Test
	public void test1()
	{
		Assertions.assertNotNull(constructorParameter);
	}
	
	@Test
	public void test2()
	{
		Assertions.assertTrue(constructorParameter != null && constructorParameter.startsWith("test-value-"));
	}
}