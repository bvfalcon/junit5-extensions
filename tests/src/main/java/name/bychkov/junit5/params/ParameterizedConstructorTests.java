package name.bychkov.junit5.params;

import static java.lang.String.format;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.AnnotationConsumerInitializer;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.commons.util.ReflectionUtils;

import name.bychkov.junit5.AbstractTests;
import name.bychkov.junit5.params.ParameterizedConstructorAnnotationProcessor.ParameterizedConstructorObject;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.MethodCall;

public class ParameterizedConstructorTests extends AbstractTests
{
	private static final Logger LOG = LoggerFactory.getLogger(ParameterizedConstructorTests.class);
	
	@TestFactory
	public Collection<DynamicContainer> tests()
	{
		Collection<DynamicContainer> testContainers = new ArrayList<>();
		Collection<Serializable> annotationClasses = readFile(ParameterizedConstructorAnnotationProcessor.DATA_FILE_LOCATION);
		for (Serializable item : annotationClasses)
		{
			ParameterizedConstructorObject obj = (ParameterizedConstructorObject) item;
			Class<?> targetClass = ReflectionUtils.tryToLoadClass(obj.targetClass).getOrThrow(
					cause -> new JUnitException(format("Could not load class [%s]", obj.targetClass), cause));
			if (AnnotationUtils.isAnnotated(targetClass, Disabled.class))
			{
				continue;
			}
			Class<?>[] params = resolveParameterTypes(obj.parameters);
			
			List<Method> testMethods = getTestMethods(targetClass);
			List<Method> beforeEachMethods = ReflectionUtils.findMethods(targetClass,
					method -> method.isAnnotationPresent(org.junit.jupiter.api.BeforeEach.class));
			List<Method> afterEachMethods = ReflectionUtils.findMethods(targetClass,
					method -> method.isAnnotationPresent(org.junit.jupiter.api.AfterEach.class));
			try
			{
				Constructor<?> constructor = targetClass.getDeclaredConstructor(params);
				List<Arguments> arguments = getArguments(constructor);
				
				Constructor<?> subtypeConstructor = generateSubtype(targetClass, params);
				for (int i = 0; i < arguments.size(); i++)
				{
					Arguments argumentsItem = arguments.get(i);
					List<DynamicTest> tests = new ArrayList<>(testMethods.size());
					for (Method testMethod : testMethods)
					{
						DynamicTest test = DynamicTest.dynamicTest(testMethod.getName(),
								getExecutable(subtypeConstructor, argumentsItem, testMethod, beforeEachMethods, afterEachMethods));
						tests.add(test);
					}
					DynamicContainer testContainer = DynamicContainer.dynamicContainer(obj.targetClass + "[" + i + "] ", tests);
					testContainers.add(testContainer);
				}
			}
			catch (NoSuchMethodException | SecurityException e)
			{
				LOG.warn(e, () -> "Cannot find constructor " + obj.annotatedElement);
			}
		}
		return testContainers;
	}
	
	private Class<?>[] resolveParameterTypes(String[] parameterClassNames)
	{
		Class<?>[] result = new Class<?>[parameterClassNames.length];
		for (int i = 0; i < parameterClassNames.length; i++)
		{
			String parameter = parameterClassNames[i];
			Class<?> parameterClass = ReflectionUtils.tryToLoadClass(parameter).getOrThrow(
					cause -> new JUnitException(format("Could not load class [%s]", parameter), cause));
			result[i] = parameterClass;
		}
		return result;
	}
	
	private org.junit.jupiter.api.function.Executable getExecutable(Constructor<?> constructor, Arguments arguments, Method testMethod,
			List<Method> beforeEachMethods, List<Method> afterEachMethods)
	{
		return () ->
		{
			Object testClassInstance = ReflectionUtils.newInstance(constructor, arguments.get());
			try
			{
				for (Method beforeEachMethod : beforeEachMethods)
				{
					ReflectionUtils.invokeMethod(beforeEachMethod, testClassInstance);
				}
				ReflectionUtils.invokeMethod(testMethod, testClassInstance);
			}
			catch (AssertionError e)
			{
				e.printStackTrace(System.err);
				throw e;
			}
			catch (Throwable e)
			{
				e.printStackTrace(System.out);
				throw e;
			}
			finally
			{
				for (Method afterEachMethod : afterEachMethods)
				{
					ReflectionUtils.invokeMethod(afterEachMethod, testClassInstance);
				}
			}
		};
	}
	
	private List<Arguments> getArguments(Constructor<?> constructor)
	{
		ExtensionContext extensionContext = new ParameterizedConstructorExecutionContext(constructor);
		List<ArgumentsSource> argumentSources = AnnotationUtils.findRepeatableAnnotations(constructor, ArgumentsSource.class);
		List<Arguments> arguments = argumentSources.stream().map(ArgumentsSource::value).map(this::instantiateArgumentsProvider)
				.map(provider -> AnnotationConsumerInitializer.initialize(constructor, provider))
				.flatMap(provider -> arguments(provider, extensionContext))
				.collect(Collectors.toList());
		return arguments;
	}
	
	private Constructor<?> generateSubtype(Class<?> klass, Class<?>[] params) throws NoSuchMethodException, SecurityException
	{
		Class<?> constructedClass = new ByteBuddy().subclass(klass, ConstructorStrategy.Default.NO_CONSTRUCTORS)
				.defineConstructor(Visibility.PUBLIC).withParameters(params)
				.intercept(MethodCall.invoke(klass.getConstructor(params)).withAllArguments())
				.make().load(klass.getClassLoader()).getLoaded();
		return constructedClass.getConstructor(params);
	}
	
	private List<Method> getTestMethods(Class<?> klass)
	{
		List<Method> testMethods = ReflectionUtils.findMethods(klass,
				method -> method.isAnnotationPresent(org.junit.jupiter.api.Test.class)
						&& !method.isAnnotationPresent(org.junit.jupiter.api.Disabled.class));
		return testMethods;
	}
	
	private ArgumentsProvider instantiateArgumentsProvider(Class<? extends ArgumentsProvider> clazz)
	{
		try
		{
			return ReflectionUtils.newInstance(clazz);
		}
		catch (Exception ex)
		{
			if (ex instanceof NoSuchMethodException)
			{
				String message = String.format("Failed to find a no-argument constructor for ArgumentsProvider [%s]. "
						+ "Please ensure that a no-argument constructor exists and "
						+ "that the class is either a top-level class or a static nested class",
						clazz.getName());
				throw new JUnitException(message, ex);
			}
			throw ex;
		}
	}
	
	protected static Stream<? extends Arguments> arguments(ArgumentsProvider provider, ExtensionContext context)
	{
		try
		{
			return provider.provideArguments(context);
		}
		catch (Exception e)
		{
			throw ExceptionUtils.throwAsUncheckedException(e);
		}
	}
}