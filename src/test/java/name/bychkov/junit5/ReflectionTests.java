package name.bychkov.junit5;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.opentest4j.AssertionFailedError;

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
		Collection<Object> annotationClasses = readFile();
		
		for (Object item : annotationClasses)
		{
			DynamicTest test = null;
			if (item instanceof CheckAnnotationProcessor.CheckConstructorObject)
			{
				CheckAnnotationProcessor.CheckConstructorObject constructorObject = (CheckAnnotationProcessor.CheckConstructorObject) item;
				String messagePrefix = String.format("Annotation @%s on type %s warns:", CheckConstructor.class.getSimpleName(), constructorObject.annotatedElement);
				test = getDynamicConstructorTest(constructorObject, messagePrefix);
			}
			else if (item instanceof CheckAnnotationProcessor.CheckFieldObject)
			{
				CheckAnnotationProcessor.CheckFieldObject fieldObject = (CheckAnnotationProcessor.CheckFieldObject) item;
				String messagePrefix = String.format("Annotation @%s on field %s warns:", CheckField.class.getSimpleName(), fieldObject.annotatedElement);
				test = getDynamicFieldTest(fieldObject, messagePrefix);
			}
			else if (item instanceof CheckAnnotationProcessor.CheckMethodObject)
			{
				CheckAnnotationProcessor.CheckMethodObject methodObject = (CheckAnnotationProcessor.CheckMethodObject) item;
				String messagePrefix = String.format("Annotation @%s on method %s warns:", CheckMethod.class.getSimpleName(), methodObject.annotatedElement);
				test = getDynamicMethodTest(methodObject, messagePrefix);
			}
			if (test != null)
			{
				tests.add(test);
			}
		}
		
		return tests;
	}
	
	private Collection<Object> readFile()
	{
		try (InputStream is = getClass().getClassLoader().getResourceAsStream(CheckAnnotationProcessor.dataFileLocation);
				ByteArrayInputStream bais = new ByteArrayInputStream(is.readAllBytes());
				ObjectInput in = new ObjectInputStream(bais))
		{
			Set result = (Set) in.readObject();
			return result;
		}
		catch (ClassNotFoundException | IOException e)
		{
			e.printStackTrace();
			return new HashSet<>();
		}
	}
	
	private DynamicTest getDynamicFieldTest(CheckAnnotationProcessor.CheckFieldObject fieldObject, String messagePrefix)
	{
		return DynamicTest.dynamicTest("testField", () ->
		{
			try
			{
				ClassPool pool = ClassPool.getDefault();
				CtClass cc = pool.get(fieldObject.targetClass);
				cc.getField(fieldObject.value);
			}
			catch (NotFoundException e)
			{
				{
					if (fieldObject.message != null && !fieldObject.message.trim().isEmpty())
					{
						throw new AssertionFailedError(fieldObject.message);
					}
					else
					{
						throw new AssertionFailedError(String.format("%s Class %s has no accessible field %s", messagePrefix, fieldObject.targetClass, fieldObject.value));
					}
				}
			}
		});
	}
	
	private DynamicTest getDynamicConstructorTest(CheckAnnotationProcessor.CheckConstructorObject constructorObject, String messagePrefix)
	{
		return DynamicTest.dynamicTest("testConstructor", () ->
		{
			try
			{
				ClassPool pool = ClassPool.getDefault();
				CtClass cc = pool.get(constructorObject.targetClass);
				CtClass[] parameterCtClasses = transformParameters(constructorObject.parameters);
				cc.getConstructor(Descriptor.ofConstructor(parameterCtClasses));
			}
			catch (NotFoundException e)
			{
				if (constructorObject.message != null && !constructorObject.message.trim().isEmpty())
				{
					throw new AssertionFailedError(constructorObject.message);
				}
				else
				{
					throw new AssertionFailedError(String.format("%s Class %s has no accessible constructor %s", messagePrefix, constructorObject.targetClass,
							constructorObject.parameters.length == 0 ? "without parameters" : "with parameters " + Stream.of(constructorObject.parameters).collect(Collectors.joining(", "))), e);
				}
			}
		});
	}
	
	private CtClass[] transformParameters(String[] parameterClassNames)
	{
		ClassPool pool = ClassPool.getDefault();
		return Stream.of(parameterClassNames).map(pcn -> {
			try
			{
				return pool.get(pcn);
			}
			catch (NotFoundException e)
			{
				throw new RuntimeException(e.getMessage(), e);
			}
		}).toArray(CtClass[]::new);
	}
	
	private DynamicTest getDynamicMethodTest(CheckAnnotationProcessor.CheckMethodObject methodObject, String messagePrefix)
	{
		return DynamicTest.dynamicTest("testMethod", () ->
		{
			try
			{
				ClassPool pool = ClassPool.getDefault();
				CtClass cc = pool.get(methodObject.targetClass);
				CtClass returnClass = pool.get(methodObject.returnType);
				CtClass[] parameterCtClasses = transformParameters(methodObject.parameters);
				cc.getMethod(methodObject.value, Descriptor.ofMethod(returnClass, parameterCtClasses));
			}
			catch (NotFoundException e)
			{
				if (methodObject.message != null && !methodObject.message.trim().isEmpty())
				{
					throw new AssertionFailedError(methodObject.message);
				}
				else
				{
					throw new AssertionFailedError(String.format("%s Class %s has no accessible method %s %s(%s)",
							messagePrefix, methodObject.targetClass, methodObject.returnType, methodObject.value,
							methodObject.parameters.length == 0 ? "" : String.join(", ", methodObject.parameters)));
				}
			}
		});
	}
}