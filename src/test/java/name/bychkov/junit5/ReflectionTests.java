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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.opentest4j.AssertionFailedError;
import org.opentest4j.TestAbortedException;
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
		
		// TYPE
		Set<Class<?>> typesAnnotatedWithConstructor = reflections.getTypesAnnotatedWith(CheckConstructor.List.class);
		typesAnnotatedWithConstructor.addAll(reflections.getTypesAnnotatedWith(CheckConstructor.class));
		for (Class<?> item : typesAnnotatedWithConstructor)
		{
			String messagePrefix = String.format("Annotation @%s on type %s warns:", CheckConstructor.class.getSimpleName(), item.getCanonicalName());
			List<CheckConstructor> list = readConstructorAnnotations(item);
			tests.addAll(getDynamicConstructorTests(list, messagePrefix));
		}
		
		Set<Class<?>> typesAnnotatedWithField = reflections.getTypesAnnotatedWith(CheckField.List.class);
		typesAnnotatedWithField.addAll(reflections.getTypesAnnotatedWith(CheckField.class));
		for (Class<?> item : typesAnnotatedWithField)
		{
			String messagePrefix = String.format("Annotation @%s on type %s warns:", CheckField.class.getSimpleName(), item.getCanonicalName());
			List<CheckField> list = readFieldAnnotations(item);
			tests.addAll(getDynamicFieldTests(list, messagePrefix));
		}
		
		Set<Class<?>> typesAnnotatedWithMethod = reflections.getTypesAnnotatedWith(CheckMethod.List.class);
		typesAnnotatedWithMethod.addAll(reflections.getTypesAnnotatedWith(CheckMethod.class));
		for (Class<?> item : typesAnnotatedWithMethod)
		{
			String messagePrefix = String.format("Annotation @%s on type %s warns:", CheckMethod.class.getSimpleName(), item.getCanonicalName());
			List<CheckMethod> list = readMethodAnnotations(item);
			tests.addAll(getDynamicMethodTests(list, messagePrefix));
		}
		
		// CONSTRUCTOR
		Set<Constructor> constructorsAnnotatedWithConstructor = reflections.getConstructorsAnnotatedWith(CheckConstructor.List.class);
		constructorsAnnotatedWithConstructor.addAll(reflections.getConstructorsAnnotatedWith(CheckConstructor.class));
		for (Constructor item : constructorsAnnotatedWithConstructor)
		{
			String messagePrefix = String.format("Annotation @%s on constructor %s warns:", CheckConstructor.class.getSimpleName(), item.getName());
			List<CheckConstructor> list = readConstructorAnnotations(item);
			tests.addAll(getDynamicConstructorTests(list, messagePrefix));
		}
		
		Set<Constructor> constructorsAnnotatedWithField = reflections.getConstructorsAnnotatedWith(CheckField.List.class);
		constructorsAnnotatedWithField.addAll(reflections.getConstructorsAnnotatedWith(CheckField.class));
		for (Constructor item : constructorsAnnotatedWithField)
		{
			String messagePrefix = String.format("Annotation @%s on constructor %s warns:", CheckField.class.getSimpleName(), item.getName());
			List<CheckField> list = readFieldAnnotations(item);
			tests.addAll(getDynamicFieldTests(list, messagePrefix));
		}
		
		Set<Constructor> constructorsAnnotatedWithMethod = reflections.getConstructorsAnnotatedWith(CheckMethod.List.class);
		constructorsAnnotatedWithMethod.addAll(reflections.getConstructorsAnnotatedWith(CheckMethod.class));
		for (Constructor item : constructorsAnnotatedWithMethod)
		{
			String messagePrefix = String.format("Annotation @%s on constructor %s warns:", CheckMethod.class.getSimpleName(), item.getName());
			List<CheckMethod> list = readMethodAnnotations(item);
			tests.addAll(getDynamicMethodTests(list, messagePrefix));
		}
		
		// METHOD
		Set<Method> methodsAnnotatedWithConstructor = reflections.getMethodsAnnotatedWith(CheckConstructor.List.class);
		methodsAnnotatedWithConstructor.addAll(reflections.getMethodsAnnotatedWith(CheckConstructor.class));
		for (Method item : methodsAnnotatedWithConstructor)
		{
			String messagePrefix = String.format("Annotation @%s on method %s warns:", CheckConstructor.class.getSimpleName(), item.getName());
			List<CheckConstructor> list = readConstructorAnnotations(item);
			tests.addAll(getDynamicConstructorTests(list, messagePrefix));
		}
		
		Set<Method> methodsAnnotatedWithField = reflections.getMethodsAnnotatedWith(CheckField.List.class);
		methodsAnnotatedWithField.addAll(reflections.getMethodsAnnotatedWith(CheckField.class));
		for (Method item : methodsAnnotatedWithField)
		{
			String messagePrefix = String.format("Annotation @%s on method %s warns:", CheckField.class.getSimpleName(), item.getName());
			List<CheckField> list = readFieldAnnotations(item);
			tests.addAll(getDynamicFieldTests(list, messagePrefix));
		}
		
		Set<Method> methodsAnnotatedWithMethod = reflections.getMethodsAnnotatedWith(CheckMethod.List.class);
		methodsAnnotatedWithMethod.addAll(reflections.getMethodsAnnotatedWith(CheckMethod.class));
		for (Method item : methodsAnnotatedWithMethod)
		{
			String messagePrefix = String.format("Annotation @%s on method %s warns:", CheckMethod.class.getSimpleName(), item.getName());
			List<CheckMethod> list = readMethodAnnotations(item);
			tests.addAll(getDynamicMethodTests(list, messagePrefix));
		}
		
		// FIELD
		Set<Field> fieldsAnnotatedWithConstructor = reflections.getFieldsAnnotatedWith(CheckConstructor.List.class);
		fieldsAnnotatedWithConstructor.addAll(reflections.getFieldsAnnotatedWith(CheckConstructor.class));
		for (Field item : fieldsAnnotatedWithConstructor)
		{
			String messagePrefix = String.format("Annotation @%s on field %s warns:", CheckConstructor.class.getSimpleName(), item.getName());
			List<CheckConstructor> list = readConstructorAnnotations(item);
			tests.addAll(getDynamicConstructorTests(list, messagePrefix));
		}
		
		Set<Field> fieldsAnnotatedWithField = reflections.getFieldsAnnotatedWith(CheckField.List.class);
		fieldsAnnotatedWithField.addAll(reflections.getFieldsAnnotatedWith(CheckField.class));
		for (Field item : fieldsAnnotatedWithField)
		{
			String messagePrefix = String.format("Annotation @%s on field %s warns:", CheckField.class.getSimpleName(), item.getName());
			List<CheckField> list = readFieldAnnotations(item);
			tests.addAll(getDynamicFieldTests(list, messagePrefix));
		}
		
		Set<Field> fieldsAnnotatedWithMethod = reflections.getFieldsAnnotatedWith(CheckMethod.List.class);
		fieldsAnnotatedWithMethod.addAll(reflections.getFieldsAnnotatedWith(CheckMethod.class));
		for (Field item : fieldsAnnotatedWithMethod)
		{
			String messagePrefix = String.format("Annotation @%s on field %s warns:", CheckMethod.class.getSimpleName(), item.getName());
			List<CheckMethod> list = readMethodAnnotations(item);
			tests.addAll(getDynamicMethodTests(list, messagePrefix));
		}
		
		return tests;
	}
	
	private List<CheckConstructor> readConstructorAnnotations(Class<?> type)
	{
		List<CheckConstructor> result = new ArrayList<>();
		CheckConstructor.List wrapper = type.getAnnotation(CheckConstructor.List.class);
		if (wrapper != null)
		{
			result.addAll(Arrays.asList(wrapper.value()));
		}
		else
		{
			CheckConstructor annotation2 = type.getAnnotation(CheckConstructor.class);
			if (annotation2 != null)
			{
				result.add(annotation2);
			}
		}
		return result;
	}
	
	private List<CheckConstructor> readConstructorAnnotations(Executable executable)
	{
		List<CheckConstructor> result = new ArrayList<>();
		CheckConstructor.List wrapper = executable.getAnnotation(CheckConstructor.List.class);
		if (wrapper != null)
		{
			result.addAll(Arrays.asList(wrapper.value()));
		}
		else
		{
			CheckConstructor annotation2 = executable.getAnnotation(CheckConstructor.class);
			if (annotation2 != null)
			{
				result.add(annotation2);
			}
		}
		return result;
	}
	
	private List<CheckConstructor> readConstructorAnnotations(Field field)
	{
		List<CheckConstructor> result = new ArrayList<>();
		CheckConstructor.List wrapper = field.getAnnotation(CheckConstructor.List.class);
		if (wrapper != null)
		{
			result.addAll(Arrays.asList(wrapper.value()));
		}
		else
		{
			CheckConstructor annotation2 = field.getAnnotation(CheckConstructor.class);
			if (annotation2 != null)
			{
				result.add(annotation2);
			}
		}
		return result;
	}
	
	private List<CheckField> readFieldAnnotations(Class<?> type)
	{
		List<CheckField> result = new ArrayList<>();
		CheckField.List wrapper = type.getAnnotation(CheckField.List.class);
		if (wrapper != null)
		{
			result.addAll(Arrays.asList(wrapper.value()));
		}
		else
		{
			CheckField annotation2 = type.getAnnotation(CheckField.class);
			if (annotation2 != null)
			{
				result.add(annotation2);
			}
		}
		return result;
	}
	
	private List<CheckField> readFieldAnnotations(Executable executable)
	{
		List<CheckField> result = new ArrayList<>();
		CheckField.List wrapper = executable.getAnnotation(CheckField.List.class);
		if (wrapper != null)
		{
			result.addAll(Arrays.asList(wrapper.value()));
		}
		else
		{
			CheckField annotation2 = executable.getAnnotation(CheckField.class);
			if (annotation2 != null)
			{
				result.add(annotation2);
			}
		}
		return result;
	}
	
	private List<CheckField> readFieldAnnotations(Field field)
	{
		List<CheckField> result = new ArrayList<>();
		CheckField.List wrapper = field.getAnnotation(CheckField.List.class);
		if (wrapper != null)
		{
			result.addAll(Arrays.asList(wrapper.value()));
		}
		else
		{
			CheckField annotation2 = field.getAnnotation(CheckField.class);
			if (annotation2 != null)
			{
				result.add(annotation2);
			}
		}
		return result;
	}
	
	private List<CheckMethod> readMethodAnnotations(Class<?> type)
	{
		List<CheckMethod> result = new ArrayList<>();
		CheckMethod.List wrapper = type.getAnnotation(CheckMethod.List.class);
		if (wrapper != null)
		{
			result.addAll(Arrays.asList(wrapper.value()));
		}
		else
		{
			CheckMethod annotation2 = type.getAnnotation(CheckMethod.class);
			if (annotation2 != null)
			{
				result.add(annotation2);
			}
		}
		return result;
	}
	
	private List<CheckMethod> readMethodAnnotations(Executable executable)
	{
		List<CheckMethod> result = new ArrayList<>();
		CheckMethod.List wrapper = executable.getAnnotation(CheckMethod.List.class);
		if (wrapper != null)
		{
			result.addAll(Arrays.asList(wrapper.value()));
		}
		else
		{
			CheckMethod annotation2 = executable.getAnnotation(CheckMethod.class);
			if (annotation2 != null)
			{
				result.add(annotation2);
			}
		}
		return result;
	}
	
	private List<CheckMethod> readMethodAnnotations(Field field)
	{
		List<CheckMethod> result = new ArrayList<>();
		CheckMethod.List wrapper = field.getAnnotation(CheckMethod.List.class);
		if (wrapper != null)
		{
			result.addAll(Arrays.asList(wrapper.value()));
		}
		else
		{
			CheckMethod annotation2 = field.getAnnotation(CheckMethod.class);
			if (annotation2 != null)
			{
				result.add(annotation2);
			}
		}
		return result;
	}
	
	private Collection<DynamicTest> getDynamicConstructorTests(List<CheckConstructor> list, String messagePrefix)
	{
		Collection<DynamicTest> result = new ArrayList<>(list.size());
		for (CheckConstructor annotation : list)
		{
			Class<?> targetClass = annotation.targetClass();
			if (targetClass == null)
			{
				throw new TestAbortedException(String.format("Annotation @%s must define the attribute targetClass", CheckConstructor.class.getSimpleName()));
			}
			
			Class<?>[] targetConstructorParameters;
			if ((targetConstructorParameters = annotation.parameters()) != null && targetConstructorParameters.length != 0)
			{
				result.add(getDynamicConstructorTest(targetClass, annotation.message(), messagePrefix, targetConstructorParameters));
			}
			else
			{
				result.add(getDynamicConstructorTest(targetClass, annotation.message(), messagePrefix));
			}
		}
		return result;
	}
	
	private Collection<DynamicTest> getDynamicMethodTests(List<CheckMethod> list, String messagePrefix)
	{
		Collection<DynamicTest> result = new ArrayList<>(list.size());
		for (CheckMethod annotation : list)
		{
			Class<?> targetClass = annotation.targetClass();
			if (targetClass == null)
			{
				throw new TestAbortedException(String.format("Annotation @%s must define the attribute targetClass", CheckMethod.class.getSimpleName()));
			}
			
			String targetMethod = annotation.value();
			if (targetMethod != null && !targetMethod.trim().isEmpty())
			{
				Class<?>[] targetMethodParameters = annotation.parameters();
				result.add(getDynamicMethodTest(targetClass, targetMethod, annotation.message(), messagePrefix, targetMethodParameters));
			}
			else
			{
				throw new TestAbortedException(String.format("Annotation @%s must define the attribute value", CheckMethod.class.getSimpleName()));
			}
		}
		return result;
	}
	
	private Collection<DynamicTest> getDynamicFieldTests(List<CheckField> list, String messagePrefix)
	{
		Collection<DynamicTest> result = new ArrayList<>(list.size());
		for (CheckField annotation : list)
		{
			Class<?> targetClass = annotation.targetClass();
			if (targetClass == null)
			{
				throw new TestAbortedException(String.format("Annotation @%s must define the attribute targetClass", CheckField.class.getSimpleName()));
			}
			
			String targetField = annotation.value();
			if (targetField != null && !targetField.trim().isEmpty())
			{
				result.add(getDynamicFieldTest(targetClass, targetField, annotation.message(), messagePrefix));
			}
			else
			{
				throw new TestAbortedException(String.format("Annotation @%s must define the attribute value", CheckField.class.getSimpleName()));
			}
		}
		return result;
	}
	
	private DynamicTest getDynamicFieldTest(Class<?> targetClass, String targetField, String userMessage, String messagePrefix)
	{
		return DynamicTest.dynamicTest("testField", () ->
		{
			Field field = FieldUtils.getField(targetClass, targetField, true);
			if (field == null)
			{
				if (userMessage != null && !userMessage.trim().isEmpty())
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
	
	private DynamicTest getDynamicConstructorTest(Class<?> targetClass, String userMessage, String messagePrefix, Class<?>... parameterClasses)
	{
		return DynamicTest.dynamicTest("testConstructor", () ->
		{
			try
			{
				targetClass.getDeclaredConstructor(parameterClasses);
			}
			catch (NoSuchMethodException | SecurityException e)
			{
				if (userMessage != null && !userMessage.trim().isEmpty())
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
	
	private DynamicTest getDynamicMethodTest(Class<?> targetClass, String targetMethod, String userMessage, String messagePrefix, Class<?>... parameterClasses)
	{
		return DynamicTest.dynamicTest("testMethod", () ->
		{
			Method method = MethodUtils.getAccessibleMethod(targetClass, targetMethod, parameterClasses);
			if (method == null)
			{
				if (userMessage != null && !userMessage.trim().isEmpty())
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