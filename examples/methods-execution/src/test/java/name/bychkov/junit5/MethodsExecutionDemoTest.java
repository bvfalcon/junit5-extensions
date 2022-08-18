package name.bychkov.junit5;

import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@MethodsExecution(ExecutionMode.CONCURRENT)
public class MethodsExecutionDemoTest
{
	private int test;
	
	@Test
	public void test1()
	{
		System.out.println("test1");
	}
	
	@Test
	@Execution(ExecutionMode.SAME_THREAD)
	public void test2()
	{
		System.out.println("test2");
	}
	
	public static IntStream testData()
	{
		return IntStream.range(1, 1001);
	}
	
	protected void testMethod() {}
	
	@ParameterizedTest
	@MethodSource(value = "testData")
	public void prameterizedTest(int parameter)
	{
		System.out.println(parameter);
	}
}
