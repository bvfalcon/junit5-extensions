package name.bychkov.junit5;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.opentest4j.AssertionFailedError;
import org.opentest4j.TestAbortedException;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.Descriptor;

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
		return readAnnotations(() -> type.getAnnotation(CheckConstructor.class),
				() -> Optional.ofNullable(type.getAnnotation(CheckConstructor.List.class)).map(o -> o.value()).orElse(new CheckConstructor[0]));
	}
	
	private List<CheckConstructor> readConstructorAnnotations(Executable executable)
	{
		return readAnnotations(() -> executable.getAnnotation(CheckConstructor.class),
				() -> Optional.ofNullable(executable.getAnnotation(CheckConstructor.List.class)).map(o -> o.value()).orElse(new CheckConstructor[0]));
	}
	
	private List<CheckConstructor> readConstructorAnnotations(Field field)
	{
		return readAnnotations(() -> field.getAnnotation(CheckConstructor.class),
				() -> Optional.ofNullable(field.getAnnotation(CheckConstructor.List.class)).map(o -> o.value()).orElse(new CheckConstructor[0]));
	}
	
	private List<CheckField> readFieldAnnotations(Class<?> type)
	{
		return readAnnotations(() -> type.getAnnotation(CheckField.class),
				() -> Optional.ofNullable(type.getAnnotation(CheckField.List.class)).map(o -> o.value()).orElse(new CheckField[0]));
	}
	
	private List<CheckField> readFieldAnnotations(Executable executable)
	{
		return readAnnotations(() -> executable.getAnnotation(CheckField.class),
				() -> Optional.ofNullable(executable.getAnnotation(CheckField.List.class)).map(o -> o.value()).orElse(new CheckField[0]));
	}
	
	private List<CheckField> readFieldAnnotations(Field field)
	{
		return readAnnotations(() -> field.getAnnotation(CheckField.class),
				() -> Optional.ofNullable(field.getAnnotation(CheckField.List.class)).map(o -> o.value()).orElse(new CheckField[0]));
	}
	
	private List<CheckMethod> readMethodAnnotations(Class<?> type)
	{
		return readAnnotations(() -> type.getAnnotation(CheckMethod.class),
				() -> Optional.ofNullable(type.getAnnotation(CheckMethod.List.class)).map(o -> o.value()).orElse(new CheckMethod[0]));
	}
	
	private List<CheckMethod> readMethodAnnotations(Executable executable)
	{
		return readAnnotations(() -> executable.getAnnotation(CheckMethod.class),
				() -> Optional.ofNullable(executable.getAnnotation(CheckMethod.List.class)).map(o -> o.value()).orElse(new CheckMethod[0]));
	}
	
	private <T extends Annotation> List<T> readAnnotations(Supplier<T> annotationGetter, Supplier<T[]> arrayAnnotationGetter)
	{
		List<T> result = new ArrayList<>();
		result.addAll(Arrays.asList(arrayAnnotationGetter.get()));
		T annotation = annotationGetter.get();
		if (annotation != null)
		{
			result.add(annotation);
		}
		return result;
	}
	
	private List<CheckMethod> readMethodAnnotations(Field field)
	{
		return readAnnotations(() -> field.getAnnotation(CheckMethod.class),
				() -> Optional.ofNullable(field.getAnnotation(CheckMethod.List.class)).map(o -> o.value()).orElse(new CheckMethod[0]));
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
				result.add(getDynamicMethodTest(targetClass, targetMethod, annotation.message(), messagePrefix, annotation.returnType(), targetMethodParameters));
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
			try
			{
				ClassPool pool = ClassPool.getDefault();
				CtClass cc = pool.get(targetClass.getCanonicalName());
				cc.getField(targetField);
			}
			catch (NotFoundException e)
			{
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
	
	private DynamicTest getDynamicMethodTest(Class<?> targetClass, String targetMethod, String userMessage, String messagePrefix, Class<?> returnType, Class<?>... parameterClasses)
	{
		return DynamicTest.dynamicTest("testMethod", () ->
		{
			try
			{
				ClassPool pool = ClassPool.getDefault();
				CtClass cc = pool.get(targetClass.getCanonicalName());
				CtClass returnClass = pool.get(returnType.getCanonicalName());
				CtClass[] parameterCtClasses = Stream.of(parameterClasses).map(pc -> {
					try
					{
						return pool.get(pc.getCanonicalName());
					}
					catch (NotFoundException e)
					{
						throw new RuntimeException(e.getMessage(), e);
					}
				}).toArray(CtClass[]::new);
				cc.getMethod(targetMethod, Descriptor.ofMethod(returnClass, parameterCtClasses));
			}
			catch (NotFoundException e)
			{
				if (userMessage != null && !userMessage.trim().isEmpty())
				{
					throw new AssertionFailedError(userMessage);
				}
				else
				{
					throw new AssertionFailedError(String.format("%s Class %s has no accessible method %s %s(%s)", messagePrefix, targetClass.getCanonicalName(), returnType.getCanonicalName(),
							targetMethod, parameterClasses.length == 0 ? "" : Stream.of(parameterClasses).map(Class::getCanonicalName).collect(Collectors.joining(", "))));
				}
			}
		});
	}
}