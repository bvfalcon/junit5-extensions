package name.bychkov.junit5;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import name.bychkov.junit5.params.ParameterizedConstructor;
import name.bychkov.junit5.params.provider.EmptySource;
import name.bychkov.junit5.params.provider.EnumSource;
import name.bychkov.junit5.params.provider.EnumSource.Mode;
import name.bychkov.junit5.params.provider.MethodSource;
import name.bychkov.junit5.params.provider.NullSource;
import name.bychkov.junit5.params.provider.ValueSource;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class TemplateTest
{
	private String constructorParameter;
	
	@ParameterizedConstructor
	@ValueSource(strings = { "test-value-428", "test-value-746" })
	@NullSource
	@EmptySource
	@MethodSource("testData")
	public TemplateTest(String constructorParameter)
	{
		this.constructorParameter = constructorParameter;
	}
	
	@ParameterizedConstructor
	@ValueSource(ints = { 475, 267 })
	public TemplateTest(int constructorParameter)
	{
		this.constructorParameter = Integer.toString(constructorParameter);
	}
	
	@ParameterizedConstructor
	@ValueSource(shorts = { (short) 157, (short) 349 })
	public TemplateTest(short constructorParameter)
	{
		this.constructorParameter = Short.toString(constructorParameter);
	}
	
	@ParameterizedConstructor
	@ValueSource(bytes = { (byte) 84, (byte) 4 })
	public TemplateTest(byte constructorParameter)
	{
		this.constructorParameter = Byte.toString(constructorParameter);
	}
	
	@ParameterizedConstructor
	@ValueSource(longs = { 132L, 485L })
	public TemplateTest(long constructorParameter)
	{
		this.constructorParameter = Long.toString(constructorParameter);
	}
	
	@ParameterizedConstructor
	@ValueSource(floats = { 42.4F, 97.4F })
	public TemplateTest(float constructorParameter)
	{
		this.constructorParameter = Float.toString(constructorParameter);
	}
	
	@ParameterizedConstructor
	@ValueSource(doubles = { 42.3D, 97.5D })
	public TemplateTest(double constructorParameter)
	{
		this.constructorParameter = Double.toString(constructorParameter);
	}
	
	@ParameterizedConstructor
	@ValueSource(chars = { 'f', 'p' })
	public TemplateTest(char constructorParameter)
	{
		this.constructorParameter = Character.toString(constructorParameter);
	}
	
	@ParameterizedConstructor
	@ValueSource(booleans = { true, false })
	public TemplateTest(boolean constructorParameter)
	{
		this.constructorParameter = Boolean.toString(constructorParameter);
	}
	
	@ParameterizedConstructor
	@ValueSource(classes = { Serializable.class, Number.class, InnerClass.class })
	public TemplateTest(Class<?> constructorParameter)
	{
		this.constructorParameter = constructorParameter.getSimpleName();
	}
	
	public static class InnerClass {}
	
	@ParameterizedConstructor
	@EnumSource(value = TestEnum1.class)
	public TemplateTest(TestEnum1 enumValue)
	{
		this.constructorParameter = enumValue.name();
	}
	
	@ParameterizedConstructor
	@EnumSource(names = "TEST453")
	public TemplateTest(TestEnum2 enumValue)
	{
		this.constructorParameter = enumValue.name();
	}
	
	@ParameterizedConstructor
	@EnumSource(names = "TEST854", mode = Mode.EXCLUDE)
	public TemplateTest(TestEnum3 enumValue)
	{
		this.constructorParameter = enumValue.name();
	}
	
	@ParameterizedConstructor
	@EnumSource(names = { ".*5.*", ".*2.*" }, mode = Mode.MATCH_ALL)
	public TemplateTest(TestEnum4 enumValue)
	{
		this.constructorParameter = enumValue.name();
	}
	
	@ParameterizedConstructor
	@EnumSource(names = { ".*7.*", ".*9.*" }, mode = Mode.MATCH_ANY)
	public TemplateTest(TestEnum5 enumValue)
	{
		this.constructorParameter = enumValue.name();
	}
	
	@ParameterizedConstructor
	@EnumSource(names = { ".*4.*", ".*8.*" }, mode = Mode.MATCH_NONE)
	public TemplateTest(TestEnum6 enumValue)
	{
		this.constructorParameter = enumValue.name();
	}
	
	public static Collection<String> testData()
	{
		return Arrays.asList("test-value-492", "test-value-738");
	}
	
	@Test
	public void test()
	{
		System.out.println("@Test executed. constructorParameter value: " + constructorParameter);
	}
}