package name.bychkov.junit5;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.ReflectionUtils.HierarchyTraversalMode;
import org.opentest4j.AssertionFailedError;

public class ReflectionTests
{
	@TestFactory
	public Collection<DynamicTest> testClassMembers()
	{
		Collection<DynamicTest> tests = new ArrayList<>();
		Collection<Object> annotationClasses = readFile();
		
		for (Object item : annotationClasses)
		{
			DynamicTest test = null;
			if (item instanceof CheckAnnotationProcessor.CheckConstructorObject)
			{
				CheckAnnotationProcessor.CheckConstructorObject constructorObject = (CheckAnnotationProcessor.CheckConstructorObject) item;
				final Function<Throwable, AssertionFailedError> exceptionProducer = e -> createAssertionFailedError(constructorObject.message, e,
						"Annotation @%s on type %s warns: Class %s has no accessible constructor %s",
						CheckConstructor.class.getSimpleName(), constructorObject.annotatedElement, constructorObject.targetClass,
						constructorObject.parameters.length == 0 ? "without parameters" : "with parameters " + Stream.of(constructorObject.parameters).collect(Collectors.joining(", ")));
				test = getDynamicConstructorTest(constructorObject, exceptionProducer);
			}
			else if (item instanceof CheckAnnotationProcessor.CheckFieldObject)
			{
				CheckAnnotationProcessor.CheckFieldObject fieldObject = (CheckAnnotationProcessor.CheckFieldObject) item;
				final Function<Throwable, AssertionFailedError> exceptionProducer = e -> createAssertionFailedError(fieldObject.message, e,
						"Annotation @%s on field %s warns: Class %s has no accessible field %s%s",
						CheckField.class.getSimpleName(), fieldObject.annotatedElement, fieldObject.targetClass,
						Optional.ofNullable(fieldObject.type).map(o -> o + " ").orElse(""), fieldObject.value);
				test = getDynamicFieldTest(fieldObject, exceptionProducer);
			}
			else if (item instanceof CheckAnnotationProcessor.CheckMethodObject)
			{
				CheckAnnotationProcessor.CheckMethodObject methodObject = (CheckAnnotationProcessor.CheckMethodObject) item;
				final Function<Throwable, AssertionFailedError> exceptionProducer = e -> createAssertionFailedError(methodObject.message, e,
						"Annotation @%s on method %s warns: Class %s has no accessible method %s %s(%s)",
						CheckMethod.class.getSimpleName(), methodObject.annotatedElement,
						methodObject.targetClass, Optional.ofNullable(methodObject.returnType).map(o -> o + " ").orElse(""),
						methodObject.value, methodObject.parameters.length == 0 ? "" : String.join(", ", methodObject.parameters));
				test = getDynamicMethodTest(methodObject, exceptionProducer);
			}
			if (test != null)
			{
				tests.add(test);
			}
		}
		
		return tests;
	}
	
	@SuppressWarnings("unchecked")
	private Collection<Object> readFile()
	{
		try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(CheckAnnotationProcessor.dataFileLocation))
		{
			final int bufLen = 4 * 0x400; // 4KB
			byte[] buf = new byte[bufLen];
			int readLen;
			try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream())
			{
				while ((readLen = inputStream.read(buf, 0, bufLen)) != -1)
					outputStream.write(buf, 0, readLen);
				
				byte[] bytes = outputStream.toByteArray();
				ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
				ObjectInput in = new ObjectInputStream(bais);
				return (Collection<Object>) in.readObject();
			}
		}
		catch (Throwable e)
		{
			// e.printStackTrace();
			return new HashSet<>();
		}
	}
	
	private DynamicTest getDynamicFieldTest(CheckAnnotationProcessor.CheckFieldObject fieldObject, Function<Throwable, AssertionFailedError> exceptionProducer)
	{
		return DynamicTest.dynamicTest("testField", () ->
		{
			try
			{
				Class<?> targetClass = Class.forName(fieldObject.targetClass);
				Predicate<Field> predicate = candidate ->
				{
					if (!Objects.equals(fieldObject.value, candidate.getName()))
					{
						return false;
					}
					if (fieldObject.type != null && !fieldObject.type.trim().isEmpty()
							&& !Objects.equals(fieldObject.type, candidate.getType().getCanonicalName()))
					{
						return false;
					}
					return true;
				};
				List<Field> fields = ReflectionUtils.findFields(targetClass, predicate, HierarchyTraversalMode.TOP_DOWN);
				if (fields.isEmpty())
				{
					throw exceptionProducer.apply(null);
				}
			}
			catch (Throwable e)
			{
				throw exceptionProducer.apply(e);
			}
		});
	}
	
	private DynamicTest getDynamicConstructorTest(CheckAnnotationProcessor.CheckConstructorObject constructorObject, Function<Throwable, AssertionFailedError> exceptionProducer)
	{
		return DynamicTest.dynamicTest("testConstructor", () ->
		{
			try
			{
				Class<?> targetClass = Class.forName(constructorObject.targetClass);
				Predicate<Constructor<?>> predicate = candidate ->
				{
					if (!areParametersEquals(constructorObject.parameters, candidate.getParameterTypes()))
					{
						return false;
					}
					return true;
				};
				List<Constructor<?>> constructors = ReflectionUtils.findConstructors(targetClass, predicate);
				if (constructors.isEmpty())
				{
					throw exceptionProducer.apply(null);
				}
			}
			catch (Throwable e)
			{
				throw exceptionProducer.apply(e);
			}
		});
	}
	
	private AssertionFailedError createAssertionFailedError(String message, Throwable exception, String errorMessageFormat, Object... args)
	{
		if (exception != null && exception instanceof AssertionFailedError)
		{
			return (AssertionFailedError) exception;
		}
		if (message != null && !message.trim().isEmpty())
		{
			throw new AssertionFailedError(message);
		}
		else
		{
			if (exception != null)
			{
				throw new AssertionFailedError(String.format(errorMessageFormat, args), exception);
			}
			else
			{
				throw new AssertionFailedError(String.format(errorMessageFormat, args));
			}
		}
	}
	
	private boolean areParametersEquals(String[] annotationParametersClassNames, Class<?>[] candidateParameterTypes)
	{
		if (annotationParametersClassNames.length != candidateParameterTypes.length)
		{
			return false;
		}
		for (int i = 0; i < candidateParameterTypes.length; i++)
		{
			Class<?> candidateParameterType = candidateParameterTypes[i];
			if (!Objects.equals(annotationParametersClassNames[i], candidateParameterType.getCanonicalName()))
			{
				return false;
			}
		}
		return true;
	}
	
	private DynamicTest getDynamicMethodTest(CheckAnnotationProcessor.CheckMethodObject methodObject, Function<Throwable, AssertionFailedError> exceptionProducer)
	{
		return DynamicTest.dynamicTest("testMethod", () ->
		{
			try
			{
				Class<?> targetClass = Class.forName(methodObject.targetClass);
				Predicate<Method> predicate = candidate ->
				{
					if (!Objects.equals(methodObject.value, candidate.getName()))
					{
						return false;
					}
					if (methodObject.returnType != null && !methodObject.returnType.trim().isEmpty()
							&& !Objects.equals(methodObject.returnType, candidate.getReturnType().getCanonicalName()))
					{
						return false;
					}
					if (!areParametersEquals(methodObject.parameters, candidate.getParameterTypes()))
					{
						return false;
					}
					return true;
				};
				List<Method> methods = ReflectionUtils.findMethods(targetClass, predicate);
				if (methods.isEmpty())
				{
					throw exceptionProducer.apply(null);
				}
			}
			catch (Throwable e)
			{
				throw exceptionProducer.apply(e);
			}
		});
	}
}