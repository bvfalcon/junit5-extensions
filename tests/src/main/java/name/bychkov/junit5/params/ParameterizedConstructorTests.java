package name.bychkov.junit5.params;

import static java.lang.String.format;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;

import name.bychkov.junit5.AbstractTests;
import name.bychkov.junit5.params.ParameterizedConstructorAnnotationProcessor.ParameterizedConstructorObject;

public class ParameterizedConstructorTests extends AbstractTests
{
	private static final Logger LOG = LoggerFactory.getLogger(ParameterizedConstructorTests.class);
	
	@TestFactory
	public Collection<DynamicContainer> tests()
	{
		Collection<DynamicContainer> testContainers = new ArrayList<>();
		Collection<Serializable> annotationClasses = readFile(ParameterizedConstructorAnnotationProcessor.DATA_FILE_LOCATION);
		
		Map<String, List<ParameterizedConstructorObject>> annotationClassesGrouppedByTargetClass = groupByTargetClass(annotationClasses);
		for (Map.Entry<String, List<ParameterizedConstructorObject>> entry : annotationClassesGrouppedByTargetClass.entrySet())
		{
			Class<?> targetClass = ReflectionUtils.tryToLoadClass(entry.getKey()).getOrThrow(
					cause -> new JUnitException(format("Could not load class [%s]", entry.getKey()), cause));
			if (AnnotationUtils.isAnnotated(targetClass, Disabled.class))
			{
				continue;
			}
			TestInstance.Lifecycle lifecycle = getTestInstanceLifecycle(targetClass);
			
			List<Method> beforeAllMethods = ReflectionUtils.findMethods(targetClass,
					method -> AnnotationUtils.isAnnotated(method, BeforeAll.class));
			List<Method> beforeEachMethods = ReflectionUtils.findMethods(targetClass,
					method -> AnnotationUtils.isAnnotated(method, BeforeEach.class));
			List<Method> testMethods = getTestMethods(targetClass);
			List<Method> afterEachMethods = ReflectionUtils.findMethods(targetClass,
					method -> AnnotationUtils.isAnnotated(method, AfterEach.class));
			List<Method> afterAllMethods = ReflectionUtils.findMethods(targetClass,
					method -> AnnotationUtils.isAnnotated(method, AfterAll.class));
			
			try
			{
				Map<Constructor<?>, List<Arguments>> argumentsMap = getArguments(targetClass, entry.getValue());
				InstanceProducer<?> instanceProducer = new InstanceProducer<>(lifecycle, beforeAllMethods, afterAllMethods,
						argumentsMap.values().stream().map(List::size).reduce(0, Integer::sum).intValue() * testMethods.size());
				SubClassProducer<?> subclassProducer = new SubClassProducer<>(targetClass);
				
				for (Constructor<?> constructor : argumentsMap.keySet())
				{
					List<Arguments> arguments = argumentsMap.get(constructor);
					Preconditions.condition(!arguments.isEmpty(), () -> format("Annotation @%s must be used with one or more annotations @*Source",
							ParameterizedConstructor.class.getSimpleName()));
					
					@SuppressWarnings("rawtypes")
					Constructor subtypeConstructor = generateSubtype(subclassProducer, constructor.getParameterTypes());
					for (int i = 0; i < arguments.size(); i++)
					{
						Arguments argumentsItem = arguments.get(i);
						List<DynamicTest> tests = new ArrayList<>(testMethods.size());
						for (Method testMethod : testMethods)
						{
							@SuppressWarnings("unchecked")
							DynamicTest test = DynamicTest.dynamicTest(testMethod.getName(),
									getExecutable(instanceProducer, subtypeConstructor, argumentsItem, testMethod,
											beforeEachMethods, afterEachMethods));
							tests.add(test);
						}
						DynamicContainer testContainer = DynamicContainer.dynamicContainer(entry.getKey() + "[" + i + "] ", tests);
						testContainers.add(testContainer);
					}
				}
			}
			catch (NoSuchMethodException | SecurityException e)
			{
				LOG.warn(e, () -> "Error has acquired while class " + entry.getKey() + " processing");
			}
		}
		return testContainers;
	}
	
	private Map<String, List<ParameterizedConstructorObject>> groupByTargetClass(Collection<Serializable> annotationClasses)
	{
		Map<String, List<ParameterizedConstructorObject>> result = new HashMap<>();
		for (Serializable item : annotationClasses)
		{
			ParameterizedConstructorObject obj = (ParameterizedConstructorObject) item;
			result.computeIfAbsent(obj.targetClass, k -> new ArrayList<>()).add(obj);
		}
		return result;
	}
	
	static class InstanceProducer<T>
	{
		TestInstance.Lifecycle lifecycle;
		List<Method> beforeAllMethods;
		List<Method> afterAllMethods;
		AtomicInteger counter;
		AtomicBoolean beforeAllExecuted = new AtomicBoolean();
		
		public InstanceProducer(TestInstance.Lifecycle lifecycle,
				List<Method> beforeAllMethods, List<Method> afterAllMethods, int testCount)
		{
			this.lifecycle = lifecycle;
			this.beforeAllMethods = beforeAllMethods;
			this.afterAllMethods = afterAllMethods;
			this.counter = new AtomicInteger(testCount);
		}
		
		Constructor<T> constructor;
		Arguments arguments;
		private T instance;
		
		public T getInstance(Constructor<T> constructor, Arguments arguments)
		{
			synchronized (this)
			{
				if (!beforeAllExecuted.getAndSet(true))
				{
					for (Method method : beforeAllMethods)
					{
						ReflectionUtils.invokeMethod(method, null);
					}
				}
			}
			if (lifecycle == TestInstance.Lifecycle.PER_METHOD)
			{
				T testClassInstance = ReflectionUtils.newInstance(constructor, arguments.get());
				return testClassInstance;
			}
			else
			{
				synchronized (this)
				{
					if (!Objects.equals(this.constructor, constructor) || !Objects.equals(this.arguments, arguments))
					{
						this.constructor = constructor;
						this.arguments = arguments;
						this.instance = ReflectionUtils.newInstance(constructor, arguments.get());
					}
					return instance;
				}
			}
		}
		
		public void endProcessing()
		{
			if (counter.decrementAndGet() == 0)
			{
				for (Method method : afterAllMethods)
				{
					ReflectionUtils.invokeMethod(method, null);
				}
			}
		}
	}
	
	private TestInstance.Lifecycle getTestInstanceLifecycle(Class<?> targetClass)
	{
		if (AnnotationUtils.isAnnotated(targetClass, TestInstance.class))
		{
			return targetClass.getAnnotation(TestInstance.class).value();
		}
		else
		{
			return TestInstance.Lifecycle.PER_METHOD;
		}
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
	
	private <T> org.junit.jupiter.api.function.Executable getExecutable(InstanceProducer<T> instanceProducer,
			Constructor<T> constructor, Arguments arguments, Method testMethod,
			List<Method> beforeEachMethods, List<Method> afterEachMethods)
	{
		return () ->
		{
			Object testClassInstance = instanceProducer.getInstance(constructor, arguments);
			try
			{
				for (Method method : beforeEachMethods)
				{
					ReflectionUtils.invokeMethod(method, testClassInstance);
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
				try
				{
					for (Method method : afterEachMethods)
					{
						ReflectionUtils.invokeMethod(method, testClassInstance);
					}
				}
				finally
				{
					instanceProducer.endProcessing();
				}
			}
		};
	}
	
	private static Random random = new Random();
	
	private <T, E extends T> Constructor<E> generateSubtype(SubClassProducer producer, Class<?>[] params) throws NoSuchMethodException, SecurityException
	{
		Class<E> subclass = (Class<E>) producer.get(Integer.toString(random.nextInt(Integer.MAX_VALUE)), params);
		return subclass.getConstructor(params);
	}
	
	private List<Method> getTestMethods(Class<?> klass)
	{
		List<Method> testMethods = ReflectionUtils.findMethods(klass,
				method -> AnnotationUtils.isAnnotated(method, Test.class)
						&& !AnnotationUtils.isAnnotated(method, Disabled.class));
		return testMethods;
	}
	
	private Map<Constructor<?>, List<Arguments>> getArguments(Class<?> targetClass, List<ParameterizedConstructorObject> objects)
			throws NoSuchMethodException, SecurityException
	{
		Map<Constructor<?>, List<Arguments>> arguments = new HashMap<>();
		for (ParameterizedConstructorObject obj : objects)
		{
			Class<?>[] params = resolveParameterTypes(obj.parameters);
			Constructor<?> constructor = targetClass.getDeclaredConstructor(params);
			List<Arguments> itemArguments = getArguments(constructor, obj);
			arguments.put(constructor, itemArguments);
		}
		return arguments;
	}
	
	private List<Arguments> getArguments(Constructor<?> constructor, ParameterizedConstructorObject object)
	{
		ExtensionContext extensionContext = new ParameterizedConstructorExecutionContext(constructor);
		List<ParameterizedConstructorObjectAcceptor> argumentSources = new ArrayList<>();
		if (object.hasEmptySource)
		{
			argumentSources.add(new EmptyArgumentsProvider());
		}
		if (object.hasNullSource)
		{
			argumentSources.add(new NullArgumentsProvider());
		}
		if (object.hasEnumSource)
		{
			argumentSources.add(new EnumArgumentsProvider());
		}
		if (object.hasValueSource)
		{
			argumentSources.add(new ValueArgumentsProvider());
		}
		if (object.hasMethodSource)
		{
			argumentSources.add(new MethodArgumentsProvider());
		}
		
		List<Arguments> arguments = argumentSources.stream()
				.flatMap(provider -> arguments(provider, object, extensionContext))
				.collect(Collectors.toList());
		return arguments;
	}
	
	protected static Stream<? extends Arguments> arguments(ParameterizedConstructorObjectAcceptor provider,
			ParameterizedConstructorObject object, ExtensionContext context)
	{
		try
		{
			provider.accept(object);
			return provider.provideArguments(context);
		}
		catch (Exception e)
		{
			throw ExceptionUtils.throwAsUncheckedException(e);
		}
	}
}