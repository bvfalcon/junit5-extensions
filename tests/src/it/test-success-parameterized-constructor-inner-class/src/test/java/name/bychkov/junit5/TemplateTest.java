package name.bychkov.junit5;

import java.util.Arrays;
import java.util.Collection;

import org.junit.jupiter.api.Test;

import name.bychkov.junit5.params.ParameterizedConstructor;
import name.bychkov.junit5.params.provider.MethodSource;

public abstract class TemplateTest
{
	private String constructorParameter;
	
	@ParameterizedConstructor
	@MethodSource("testData")
	public TemplateTest(TestData testData)
	{
		this.constructorParameter = "test-value-" + testData.getTestNumber();
	}
	
	public static Collection<TestData> testData()
	{
		return Arrays.asList(new TestData(504), new TestData(924), new TestData(472));
	}
	
	static class TestData
	{
		private int testNumber;
		
		public TestData(int testNumber)
		{
			this.testNumber = testNumber;
		}
		
		public int getTestNumber()
		{
			return testNumber;
		}
	}
	
	@Test
	public void test()
	{
		System.out.println("@Test executed. constructorParameter value: " + constructorParameter);
	}
}