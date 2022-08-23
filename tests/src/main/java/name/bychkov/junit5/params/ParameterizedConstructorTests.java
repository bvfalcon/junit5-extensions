package name.bychkov.junit5.params;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
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
			try
			{
				targetClass = Class.forName(obj.targetClass);
			}
			catch (ClassNotFoundException e)
			{
				LOG.warn(e, () -> "Error creating test for class " + obj.targetClass);
				continue CLASS;
			}
			List<Class<?>> params = new ArrayList<>(obj.parameters.length);
			for (String parameter : obj.parameters)
			{
				try
				{
					Class<?> parameterClass = Class.forName(parameter);
					params.add(parameterClass);
				}
				catch (ClassNotFoundException e)
				{
					LOG.warn(e, () -> "Error creating test for class " + obj.targetClass);
					continue CLASS;
				}
			}
			try
			{
				List<Method> methods = getTestMethods(targetClass);
				Object instance = getInstance(targetClass, params.toArray(new Class[0]));
				List<DynamicTest> testMethods = new ArrayList<>(methods.size());
				for (Method method : methods)
				{
					System.out.println("method: " + method.getName());
					DynamicTest testMethod = DynamicTest.dynamicTest(method.getName(), () -> ReflectionUtils.invokeMethod(method, instance));
					testMethods.add(testMethod);
				}
				DynamicContainer testContainer = DynamicContainer.dynamicContainer(obj.targetClass + "[1] ", testMethods);
				tests.add(testContainer);
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
	
	private Object getInstance(Class<?> klass, Class<?>[] params) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException
	{
		Class<?> constructedClass = new ByteBuddy().subclass(klass, ConstructorStrategy.Default.NO_CONSTRUCTORS)
				.defineConstructor(Visibility.PUBLIC).withParameters(params)
				.intercept(MethodCall.invoke(klass.getConstructor(params)).withAllArguments())
				.make().load(klass.getClassLoader()).getLoaded();
		Constructor<?> constructor = constructedClass.getConstructor(params);
		Object instance = constructor.newInstance("1");
		return instance;
	}
	
	private List<Method> getTestMethods(Class<?> klass)
	{
		List<Method> testMethods = ReflectionUtils.findMethods(klass,
				method -> method.isAnnotationPresent(org.junit.jupiter.api.Test.class)
						&& !method.isAnnotationPresent(org.junit.jupiter.api.Disabled.class));
		return testMethods;
	}
}