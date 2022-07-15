package name.bychkov.junit5;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.ReflectionUtils.HierarchyTraversalMode;
import org.junit.platform.commons.util.StringUtils;
import org.opentest4j.AssertionFailedError;

public class ReflectionTests
{
	private static final Logger LOG = LoggerFactory.getLogger(ReflectionTests.class);
	
	private static final BiFunction<Throwable, CheckAnnotationProcessor.CheckConstructorObject, AssertionFailedError> constructorExceptionProducer = (e, constructorObject) ->
			createAssertionFailedError(constructorObject.message, e, "Annotation @%s on type %s warns: Class %s has no accessible constructor %s",
			CheckConstructor.class.getSimpleName(), constructorObject.annotatedElement, constructorObject.targetClass,
			constructorObject.parameters.length == 0 ? "without parameters" : "with parameters " + Stream.of(constructorObject.parameters).collect(Collectors.joining(", ")));
	
	private static final BiFunction<Throwable, CheckAnnotationProcessor.CheckFieldObject, AssertionFailedError> fieldExceptionProducer = (e, fieldObject) ->
			createAssertionFailedError(fieldObject.message, e, "Annotation @%s on field %s warns: Class %s has no accessible field %s%s",
			CheckField.class.getSimpleName(), fieldObject.annotatedElement, fieldObject.targetClass,
			Optional.ofNullable(fieldObject.type).map(o -> o + " ").orElse(""), fieldObject.value);
	
	private static final BiFunction<Throwable, CheckAnnotationProcessor.CheckMethodObject, AssertionFailedError> methodExceptionProducer = (e, methodObject) ->
			createAssertionFailedError(methodObject.message, e, "Annotation @%s on method %s warns: Class %s has no accessible method %s %s(%s)",
			CheckMethod.class.getSimpleName(), methodObject.annotatedElement,
			methodObject.targetClass, Optional.ofNullable(methodObject.returnType).map(o -> o + " ").orElse(""),
			methodObject.value, methodObject.parameters.length == 0 ? "" : String.join(", ", methodObject.parameters));
	
	@TestFactory
	public Collection<DynamicTest> testClassMembers()
	{
		Collection<DynamicTest> tests = new ArrayList<>();
		Collection<Serializable> annotationClasses = readFile();
		
		for (Serializable item : annotationClasses)
		{
			DynamicTest test = null;
			if (item instanceof CheckAnnotationProcessor.CheckConstructorObject)
			{
				CheckAnnotationProcessor.CheckConstructorObject constructorObject = (CheckAnnotationProcessor.CheckConstructorObject) item;
				test = getDynamicConstructorTest(constructorObject);
			}
			else if (item instanceof CheckAnnotationProcessor.CheckFieldObject)
			{
				CheckAnnotationProcessor.CheckFieldObject fieldObject = (CheckAnnotationProcessor.CheckFieldObject) item;
				test = getDynamicFieldTest(fieldObject);
			}
			else if (item instanceof CheckAnnotationProcessor.CheckMethodObject)
			{
				CheckAnnotationProcessor.CheckMethodObject methodObject = (CheckAnnotationProcessor.CheckMethodObject) item;
				test = getDynamicMethodTest(methodObject);
			}
			if (test != null)
			{
				tests.add(test);
			}
		}
		
		return tests;
	}
	
	@SuppressWarnings("unchecked")
	private Set<Serializable> readFile()
	{
		try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(CheckAnnotationProcessor.dataFileLocation))
		{
			if (inputStream == null)
			{
				return Collections.emptySet();
			}
			final int bufLen = 4 * 0x400; // 4KB
			byte[] buf = new byte[bufLen];
			int readLen;
			try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream())
			{
				while ((readLen = inputStream.read(buf, 0, bufLen)) != -1)
				{
					outputStream.write(buf, 0, readLen);
				}
				byte[] bytes = outputStream.toByteArray();
				ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(bytes));
				return (Set<Serializable>) in.readObject();
			}
		}
		catch (Throwable e)
		{
			LOG.info(e, () -> String.format("Error has acquired while file reading: %s", e.getMessage()));
			return Collections.emptySet();
		}
	}
	
	private DynamicTest getDynamicFieldTest(CheckAnnotationProcessor.CheckFieldObject fieldObject)
	{
		return DynamicTest.dynamicTest("testField", () ->
		{
			try
			{
				Class<?> targetClass = Class.forName(fieldObject.targetClass);
				
				Predicate<Field> predicate = candidate -> Objects.equals(fieldObject.value, candidate.getName());
				predicate.and(candidate -> StringUtils.isBlank(fieldObject.type) || Objects.equals(fieldObject.type, candidate.getType().getCanonicalName()));
				
				List<Field> fields = ReflectionUtils.findFields(targetClass, predicate, HierarchyTraversalMode.TOP_DOWN);
				if (fields.isEmpty())
				{
					throw fieldExceptionProducer.apply(null, fieldObject);
				}
			}
			catch (Throwable e)
			{
				throw fieldExceptionProducer.apply(e, fieldObject);
			}
		});
	}
	
	private DynamicTest getDynamicConstructorTest(CheckAnnotationProcessor.CheckConstructorObject constructorObject)
	{
		return DynamicTest.dynamicTest("testConstructor", () ->
		{
			try
			{
				Class<?> targetClass = Class.forName(constructorObject.targetClass);
				Predicate<Constructor<?>> predicate = candidate -> areParametersEquals(constructorObject.parameters, candidate.getParameterTypes());
				List<Constructor<?>> constructors = ReflectionUtils.findConstructors(targetClass, predicate);
				if (constructors.isEmpty())
				{
					throw constructorExceptionProducer.apply(null, constructorObject);
				}
			}
			catch (Throwable e)
			{
				throw constructorExceptionProducer.apply(e, constructorObject);
			}
		});
	}
	
	private static AssertionFailedError createAssertionFailedError(String message, Throwable exception, String errorMessageFormat, Object... args)
	{
		if (exception instanceof AssertionFailedError)
		{
			return (AssertionFailedError) exception;
		}
		if (StringUtils.isNotBlank(message))
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
	
	private DynamicTest getDynamicMethodTest(CheckAnnotationProcessor.CheckMethodObject methodObject)
	{
		return DynamicTest.dynamicTest("testMethod", () ->
		{
			try
			{
				Class<?> targetClass = Class.forName(methodObject.targetClass);
				
				Predicate<Method> predicate = candidate -> Objects.equals(methodObject.value, candidate.getName());
				predicate.and(candidate -> StringUtils.isBlank(methodObject.returnType) || Objects.equals(methodObject.returnType, candidate.getReturnType().getCanonicalName()));
				predicate.and(candidate -> areParametersEquals(methodObject.parameters, candidate.getParameterTypes()));
				
				List<Method> methods = ReflectionUtils.findMethods(targetClass, predicate);
				if (methods.isEmpty())
				{
					throw methodExceptionProducer.apply(null, methodObject);
				}
			}
			catch (Throwable e)
			{
				throw methodExceptionProducer.apply(e, methodObject);
			}
		});
	}
}