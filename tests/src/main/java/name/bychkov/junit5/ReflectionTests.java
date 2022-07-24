package name.bychkov.junit5;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.ReflectionUtils.HierarchyTraversalMode;
import org.junit.platform.commons.util.StringUtils;
import org.opentest4j.AssertionFailedError;

public class ReflectionTests extends AbstractTests
{
	private static final BiFunction<Throwable, CheckAnnotationProcessor.CheckConstructorObject, AssertionFailedError> constructorExceptionProducer = (e, constructorObject) ->
			createAssertionFailedError(constructorObject.message, e, "Annotation @%s on %s warns: Class %s has no accessible constructor %s ",
			CheckConstructor.class.getSimpleName(), constructorObject.annotatedElement, constructorObject.targetClass,
			constructorObject.parameters.length == 0 ? "without parameters" : "with parameters " + Stream.of(constructorObject.parameters).collect(Collectors.joining(", ")));
	
	private static final BiFunction<Throwable, CheckAnnotationProcessor.CheckFieldObject, AssertionFailedError> fieldExceptionProducer = (e, fieldObject) ->
			createAssertionFailedError(fieldObject.message, e, "Annotation @%s on %s warns: Class %s has no accessible field %s%s",
			CheckField.class.getSimpleName(), fieldObject.annotatedElement, fieldObject.targetClass,
			Optional.ofNullable(fieldObject.type).map(o -> o + " ").orElse(""), fieldObject.value);
	
	private static final BiFunction<Throwable, CheckFieldsObject, AssertionFailedError> fieldsExceptionProducer = (e, fieldsObject) ->
			createAssertionFailedError(fieldsObject.message, e, "Annotation @%s on %s warns: Class %s has no accessible fields %s",
			CheckFields.class.getSimpleName(), fieldsObject.annotatedElement, fieldsObject.targetClass,
			Optional.ofNullable(fieldsObject.failureValues).map(o -> Arrays.asList(o)).map(List::stream).orElseGet(Stream::empty).collect(Collectors.joining(", ")));
	
	private static final BiFunction<Throwable, CheckAnnotationProcessor.CheckMethodObject, AssertionFailedError> methodExceptionProducer = (e, methodObject) ->
			createAssertionFailedError(methodObject.message, e, "Annotation @%s on %s warns: Class %s has no accessible method %s%s%s",
			CheckMethod.class.getSimpleName(), methodObject.annotatedElement,
			methodObject.targetClass, Optional.ofNullable(methodObject.returnType).map(o -> o + " ").orElse(""),
			methodObject.value, methodObject.parameters == null ? "" : "(" + String.join(", ", methodObject.parameters) + ")");
	
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
			else if (item instanceof CheckAnnotationProcessor.CheckFieldsObject)
			{
				CheckAnnotationProcessor.CheckFieldsObject fieldsObject = (CheckAnnotationProcessor.CheckFieldsObject) item;
				test = getDynamicFieldsTest(fieldsObject);
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
	
	private DynamicTest getDynamicFieldTest(CheckAnnotationProcessor.CheckFieldObject fieldObject)
	{
		return DynamicTest.dynamicTest("testField", () ->
		{
			try
			{
				Class<?> targetClass = Class.forName(fieldObject.targetClass);
				
				Predicate<Field> predicate = candidate -> Objects.equals(fieldObject.value, candidate.getName());
				predicate = predicate.and(candidate -> StringUtils.isBlank(fieldObject.type) || Objects.equals(fieldObject.type, candidate.getType().getCanonicalName()));
				
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
	
	static class CheckFieldsObject extends CheckAnnotationProcessor.CheckFieldsObject
	{
		private static final long serialVersionUID = -3913115685302546984L;
		
		String[] failureValues;
		
		public CheckFieldsObject(CheckAnnotationProcessor.CheckFieldsObject parentObject, String[] failureValues)
		{
			this.annotatedElement = parentObject.annotatedElement;
			this.message = parentObject.message;
			this.targetClass = parentObject.targetClass;
			this.values = parentObject.values;
			this.failureValues = failureValues;
		}
	}
	
	private DynamicTest getDynamicFieldsTest(CheckAnnotationProcessor.CheckFieldsObject fieldsObject)
	{
		return DynamicTest.dynamicTest("testFields", () ->
		{
			try
			{
				Class<?> targetClass = Class.forName(fieldsObject.targetClass);
				
				List<String> failureFields = new ArrayList<>();
				for (String field : fieldsObject.values)
				{
					Predicate<Field> predicate = candidate -> Objects.equals(field, candidate.getName());
					List<Field> fields = ReflectionUtils.findFields(targetClass, predicate, HierarchyTraversalMode.TOP_DOWN);
					if (fields.isEmpty())
					{
						failureFields.add(field);
					}
				}
				if (!failureFields.isEmpty())
				{
					CheckFieldsObject newFieldsObject = new CheckFieldsObject(fieldsObject, failureFields.toArray(new String[failureFields.size()]));
					throw fieldsExceptionProducer.apply(null, newFieldsObject);
				}
			}
			catch (Throwable e)
			{
				throw fieldsExceptionProducer.apply(e, new CheckFieldsObject(fieldsObject, new String[0]));
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
				predicate = predicate.and(candidate -> StringUtils.isBlank(methodObject.returnType) || Objects.equals(methodObject.returnType, candidate.getReturnType().getCanonicalName()));
				predicate = predicate.and(candidate -> methodObject.parameters == null || areParametersEquals(methodObject.parameters, candidate.getParameterTypes()));
				
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