package name.bychkov.junit5.params;

import static java.lang.String.format;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
		Collection<DynamicContainer> tests = new ArrayList<>();
		Collection<Serializable> annotationClasses = readFile(ParameterizedConstructorAnnotationProcessor.DATA_FILE_LOCATION);
		CLASS: for (Serializable item : annotationClasses)
		{
			ParameterizedConstructorObject obj = (ParameterizedConstructorObject) item;
			Class<?> targetClass = null;
			targetClass = ReflectionUtils.tryToLoadClass(obj.targetClass).getOrThrow(
					cause -> new JUnitException(format("Could not load class [%s]", obj.targetClass), cause));
			List<Class<?>> params = new ArrayList<>(obj.parameters.length);
			for (String parameter : obj.parameters)
			{
				Class<?> parameterClass = ReflectionUtils.tryToLoadClass(parameter).getOrThrow(
						cause -> new JUnitException(format("Could not load class [%s]", parameter), cause));
				params.add(parameterClass);
			}
			try
			{
				Constructor<?> constructor = targetClass.getDeclaredConstructor(params.toArray(new Class[0]));
				ExtensionContext extensionContext = new ParameterizedConstructorExecutionContext(constructor);
				List<ArgumentsSource> argumentSources = findSourceAnnotations(targetClass, params.toArray(new Class[0]));
				List<Arguments> arguments = argumentSources.stream().map(ArgumentsSource::value).map(this::instantiateArgumentsProvider)
						.map(provider -> AnnotationConsumerInitializer.initialize(constructor, provider))
						.flatMap(provider -> arguments(provider, extensionContext))
						.collect(Collectors.toList());
				
				List<Method> methods = getTestMethods(targetClass);
				for (int i = 0; i < arguments.size(); i++)
				{
					Arguments argumentsItem = arguments.get(i);
					Object instance = getInstance(targetClass, params.toArray(new Class[0]), argumentsItem);
					List<DynamicTest> testMethods = new ArrayList<>(methods.size());
					for (Method method : methods)
					{
						DynamicTest testMethod = DynamicTest.dynamicTest(method.getName(), () -> ReflectionUtils.invokeMethod(method, instance));
						testMethods.add(testMethod);
					}
					DynamicContainer testContainer = DynamicContainer.dynamicContainer(obj.targetClass + "[" + i + "] ", testMethods);
					tests.add(testContainer);
				}
			}
			catch (NoSuchMethodException | SecurityException e)
			{
				LOG.warn(e, () -> "Cannot find constructor " + obj.annotatedElement);
				continue CLASS;
			}
			catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
			{
				LOG.warn(e, () -> "Cannot instantiate " + obj.annotatedElement);
				continue CLASS;
			}
		}
		return tests;
	}
	
	private Object getInstance(Class<?> klass, Class<?>[] params, Arguments arguments) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException
	{
		Class<?> constructedClass = new ByteBuddy().subclass(klass, ConstructorStrategy.Default.NO_CONSTRUCTORS)
				.defineConstructor(Visibility.PUBLIC).withParameters(params)
				.intercept(MethodCall.invoke(klass.getConstructor(params)).withAllArguments())
				.make().load(klass.getClassLoader()).getLoaded();
		Constructor<?> constructor = constructedClass.getConstructor(params);
		Object instance = constructor.newInstance(arguments.get()[0]);
		return instance;
	}
	
	private List<Method> getTestMethods(Class<?> klass)
	{
		List<Method> testMethods = ReflectionUtils.findMethods(klass,
				method -> method.isAnnotationPresent(org.junit.jupiter.api.Test.class)
						&& !method.isAnnotationPresent(org.junit.jupiter.api.Disabled.class));
		return testMethods;
	}
	
	private List<ArgumentsSource> findSourceAnnotations(Class<?> klass, Class<?>[] params) throws NoSuchMethodException, SecurityException
	{
		Constructor<?> constructor = klass.getDeclaredConstructor(params);
		return AnnotationUtils.findRepeatableAnnotations(constructor, ArgumentsSource.class);
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