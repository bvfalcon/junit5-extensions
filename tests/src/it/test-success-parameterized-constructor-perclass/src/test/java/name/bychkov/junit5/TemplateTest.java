package name.bychkov.junit5;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import name.bychkov.junit5.params.ParameterizedConstructor;
import name.bychkov.junit5.params.provider.ValueSource;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class TemplateTest
{
	private String constructorParameter;
	
	@ParameterizedConstructor
	@ValueSource(strings = { "test-value-294", "test-value-168" })
	public TemplateTest(String constructorParameter)
	{
		this.constructorParameter = constructorParameter;
	}
	
	@BeforeAll
	public static void init()
	{
		System.out.println("@BeforeAll callback success");
	}
	
	@BeforeEach
	public void startUp()
	{
		System.out.println("@BeforeEach callback success");
	}
	
	@AfterEach
	public void tearDown()
	{
		System.out.println("@AfterEach callback success");
	}
	
	@AfterAll
	public static void shitdown()
	{
		System.out.println("@AfterAll callback success");
	}
	
	@Test
	public void test1()
	{
		System.out.println("@Test test1 executed. constructorParameter value: " + constructorParameter);
	}
	
	@Test
	public void test2()
	{
		System.out.println("@Test test2 executed. constructorParameter value: " + constructorParameter);
	}
}