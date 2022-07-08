package name.bychkov.junit5;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.opentest4j.AssertionFailedError;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

public class ReflectionTests
{
	@TestFactory
	public Collection<DynamicTest> testClassMembers()
	{
		Collection<DynamicTest> tests = new ArrayList<>();
		
		Reflections reflections = new Reflections(new ConfigurationBuilder().forPackages("")
				.setScanners(Scanners.TypesAnnotated, Scanners.ConstructorsAnnotated, Scanners.FieldsAnnotated, Scanners.MethodsAnnotated));
		
		Set<Class<?>> annotatedTypes = reflections.getTypesAnnotatedWith(CheckExistence.List.class);
		annotatedTypes.addAll(reflections.getTypesAnnotatedWith(CheckExistence.class));
		for (Class<?> item : annotatedTypes)
		{
			String messagePrefix = String.format("Annotation @%s on type %s warns:", CheckExistence.class.getSimpleName(), item.getCanonicalName());
			List<CheckExistence> list = readAnnotations(item);
			tests.addAll(getDynamicTests(list, messagePrefix));
		}
		
		@SuppressWarnings("rawtypes")
		Set<Constructor> annotatedConstructors = reflections.getConstructorsAnnotatedWith(CheckExistence.List.class);
		annotatedConstructors.addAll(reflections.getConstructorsAnnotatedWith(CheckExistence.class));
		for (Constructor<?> item : annotatedConstructors)
		{
			String messagePrefix = String.format("Annotation @%s on constructor %s warns:", CheckExistence.class.getSimpleName(), item.getName());
			List<CheckExistence> list = readAnnotations(item);
			tests.addAll(getDynamicTests(list, messagePrefix));
		}
		
		Set<Method> annotatedMethods = reflections.getMethodsAnnotatedWith(CheckExistence.List.class);
		annotatedMethods.addAll(reflections.getMethodsAnnotatedWith(CheckExistence.class));
		for (Method item : annotatedMethods)
		{
			String messagePrefix = String.format("Annotation @%s on method %s warns:", CheckExistence.class.getSimpleName(), item.getName());
			List<CheckExistence> list = readAnnotations(item);
			tests.addAll(getDynamicTests(list, messagePrefix));
		}
		
		Set<Field> annotatedFields = reflections.getFieldsAnnotatedWith(CheckExistence.List.class);
		annotatedFields.addAll(reflections.getFieldsAnnotatedWith(CheckExistence.class));
		for (Field item : annotatedFields)
		{
			String messagePrefix = String.format("Annotation @%s on field %s warns:", CheckExistence.class.getSimpleName(), item.getName());
			List<CheckExistence> list = readAnnotations(item);
			tests.addAll(getDynamicTests(list, messagePrefix));
		}
		
		return tests;
	}
	
	private List<CheckExistence> readAnnotations(Supplier<CheckExistence.List> wrapperSupplier, Supplier<CheckExistence> annotationSupplier)
	{
		List<CheckExistence> result = new ArrayList<>();
		CheckExistence.List wrapper = wrapperSupplier.get();
		if (wrapper != null)
		{
			result.addAll(Arrays.asList(wrapper.value()));
		}
		else
		{
			CheckExistence annotation2 = annotationSupplier.get();
			if (annotation2 != null)
			{
				result.add(annotation2);
			}
		}
		return result;
	}
	
	private List<CheckExistence> readAnnotations(Field field)
	{
		return readAnnotations(() -> field.getAnnotation(CheckExistence.List.class), () -> field.getAnnotation(CheckExistence.class));
	}
	
	private List<CheckExistence> readAnnotations(Class<?> type)
	{
		return readAnnotations(() -> type.getAnnotation(CheckExistence.List.class), () -> type.getAnnotation(CheckExistence.class));
	}
	
	private List<CheckExistence> readAnnotations(Executable executable)
	{
		return readAnnotations(() -> executable.getAnnotation(CheckExistence.List.class), () -> executable.getAnnotation(CheckExistence.class));
	}
	
	private Collection<DynamicTest> getDynamicTests(List<CheckExistence> list, String messagePrefix)
	{
		Collection<DynamicTest> result = new ArrayList<>(list.size());
		for (CheckExistence annotation : list)
		{
			Class<?> targetClass = annotation.targetClass();
			
			String targetMethod, targetField;
			Class<?>[] targetConstructorParameters;
			if ((targetField = annotation.field()) != null && !targetField.isBlank())
			{
				result.add(getFieldTest(targetClass, targetField, annotation.message(), messagePrefix));
			}
			else if ((targetConstructorParameters = annotation.constructorParameters()) != null && targetConstructorParameters.length != 0)
			{
				result.add(getConstructorTest(targetClass, annotation.message(), messagePrefix, targetConstructorParameters));
			}
			else if ((targetMethod = annotation.method()) != null && !targetMethod.isBlank())
			{
				Class<?>[] targetMethodParameters = annotation.methodParameters();
				result.add(getMethodTest(targetClass, targetMethod, annotation.message(), messagePrefix, targetMethodParameters));
			}
			else
			{
				result.add(getConstructorTest(targetClass, annotation.message(), messagePrefix));
			}
		}
		return result;
	}
	
	private DynamicTest getFieldTest(Class<?> targetClass, String targetField, String userMessage, String messagePrefix)
	{
		return DynamicTest.dynamicTest("testField", () ->
		{
			Field field = FieldUtils.getField(targetClass, targetField, true);
			if (field == null)
			{
				if (userMessage != null && !userMessage.isBlank())
				{
					throw new AssertionFailedError(userMessage);
				}
				else
				{
					throw new AssertionFailedError(String.format("%s Class %s has no accessible field %s", messagePrefix, targetClass.getCanonicalName(), targetField));
				}
			}
		});
	}
	
	private DynamicTest getConstructorTest(Class<?> targetClass, String userMessage, String messagePrefix, Class<?>... parameterClasses)
	{
		return DynamicTest.dynamicTest("testConstructor", () ->
		{
			try
			{
				targetClass.getDeclaredConstructor(parameterClasses);
			}
			catch (NoSuchMethodException | SecurityException e)
			{
				if (userMessage != null && !userMessage.isBlank())
				{
					throw new AssertionFailedError(userMessage);
				}
				else
				{
					throw new AssertionFailedError(String.format("%s Class %s has no accessible constructor %s", messagePrefix, targetClass.getCanonicalName(),
							parameterClasses.length == 0 ? "without parameters" : "with parameters " + Stream.of(parameterClasses).map(Class::getCanonicalName).collect(Collectors.joining(", "))), e);
				}
			}
		});
	}
	
	private DynamicTest getMethodTest(Class<?> targetClass, String targetMethod, String userMessage, String messagePrefix, Class<?>... parameterClasses)
	{
		return DynamicTest.dynamicTest("testMethod", () ->
		{
			Method method = MethodUtils.getAccessibleMethod(targetClass, targetMethod, parameterClasses);
			if (method == null)
			{
				if (userMessage != null && !userMessage.isBlank())
				{
					throw new AssertionFailedError(userMessage);
				}
				else
				{
					throw new AssertionFailedError(String.format("%s Class %s has no accessible method %s(%s)", messagePrefix, targetClass.getCanonicalName(), targetMethod,
							parameterClasses.length == 0 ? "" : Stream.of(parameterClasses).map(Class::getCanonicalName).collect(Collectors.joining(", "))));
				}
			}
		});
	}
}