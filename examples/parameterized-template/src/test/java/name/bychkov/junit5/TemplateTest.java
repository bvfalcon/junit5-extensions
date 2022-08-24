package name.bychkov.junit5;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import name.bychkov.junit5.params.ParameterizedConstructor;
import name.bychkov.junit5.params.provider.EmptySource;
import name.bychkov.junit5.params.provider.EnumSource;
import name.bychkov.junit5.params.provider.MethodSource;
import name.bychkov.junit5.params.provider.NullAndEmptySource;
import name.bychkov.junit5.params.provider.NullSource;
import name.bychkov.junit5.params.provider.ValueSource;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.MethodCall;

public abstract class TemplateTest
{
	private String constructorParameter;
	
	@ParameterizedConstructor
	@ValueSource(strings = { "test-value-1", "test-value-2" })
	//@NullSource
	//@EmptySource
	//@NullAndEmptySource
	@MethodSource(value = {"testData", "name.bychkov.junit5.TemplateTest#testData()"})
	public TemplateTest(String constructorParameter)
	{
		this.constructorParameter = constructorParameter;
	}
	
	@EnumSource(names = { "TEST1", "TEST2" })
	public TemplateTest(TestEnum enumValue)
	{
		this.constructorParameter = enumValue.getTestValue();
	}
	
	public static Collection<String> testData()
	{
		return Arrays.asList("test-value-3", "test-value-4");
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
	
	public static void main(String... args) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException
	{
		Class<?> baseClass = TemplateTest.class;
		Class<?>[] params = { String.class };
		Class klass = new ByteBuddy().subclass(baseClass, ConstructorStrategy.Default.NO_CONSTRUCTORS)
				.defineConstructor(Visibility.PUBLIC)
				.withParameters(params)
				.intercept(MethodCall.invoke(baseClass.getConstructor(params)).withAllArguments())
				.make()
				.load(baseClass.getClassLoader())
				.getLoaded();
		Object obj = klass.getConstructor(String.class).newInstance("12345");
		System.out.println(((TemplateTest) obj).constructorParameter);
	}
}