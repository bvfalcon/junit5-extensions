package name.bychkov.junit5;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import org.junit.platform.commons.support.ModifierSupport;
import org.junit.platform.commons.util.ClassFilter;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.ReflectionUtils.HierarchyTraversalMode;
import org.opentest4j.AssertionFailedError;

public class SerializationTest extends AbstractTests
{
	private static final Logger LOG = LoggerFactory.getLogger(SerializationTest.class);
	
	private static final BiFunction<Throwable, CheckSerializableObject, AssertionFailedError> serializableExceptionProducer = (e, serializableObject) ->
			createAssertionFailedError(serializableObject.message, e, "Annotation @%s on %s warns: next classes have problems with serialization:%s%s",
			CheckSerializable.class.getSimpleName(), serializableObject.annotatedElement, System.lineSeparator(),
			Optional.ofNullable(serializableObject.failures).map(List::stream).orElseGet(Stream::empty)
				.map(item -> "\t" + item).collect(Collectors.joining(System.lineSeparator())));
	
	/* prevent circular checks */
	private Set<Class<?>> checking = new HashSet<>();
			
	@TestFactory
	public Collection<DynamicTest> testSerialization()
	{
		Collection<Serializable> annotationClasses = readFile();
		Collection<DynamicTest> tests = annotationClasses.stream().filter(CheckAnnotationProcessor.CheckSerializableObject.class::isInstance)
				.map(CheckAnnotationProcessor.CheckSerializableObject.class::cast).map(this::getDynamicTest).collect(Collectors.toList());
		return tests;
	}
	
	static class CheckSerializableObject extends CheckAnnotationProcessor.CheckSerializableObject
	{
		private static final long serialVersionUID = 7033531351868206885L;

		List<String> failures;
		
		public CheckSerializableObject(CheckAnnotationProcessor.CheckSerializableObject parentObject, List<String> failures)
		{
			this.annotatedElement = parentObject.annotatedElement;
			this.excludes = parentObject.excludes;
			this.message = parentObject.message;
			this.targetPackage = parentObject.targetPackage;
			this.failures = failures;
		}
	}
	
	private DynamicTest getDynamicTest(CheckAnnotationProcessor.CheckSerializableObject serializableObject)
	{
		return DynamicTest.dynamicTest("testSerializable", () ->
		{
			String packageName = Objects.toString(serializableObject.targetPackage, serializableObject.annotatedElement);
			Predicate<Class<?>> predicate = candidate -> serializableObject.excludes == null || !Arrays.asList(serializableObject.excludes).contains(candidate.getCanonicalName());
			List<Class<?>> classes = ReflectionUtils.findAllClassesInPackage(packageName, ClassFilter.of(predicate));
			
			List<String> failures = new ArrayList<>();
			for (Class<?> klass : classes)
			{
				checking.add(klass);
				List<String> messages = isClassSerializable(klass);
				if (!messages.isEmpty())
				{
					failures.addAll(messages);
				}
				checking.remove(klass);
			}
			if (!failures.isEmpty())
			{
				CheckSerializableObject newSrlObject = new CheckSerializableObject(serializableObject, failures);
				throw serializableExceptionProducer.apply(null, newSrlObject);
			}
		});
	}
	
	private List<String> isClassSerializable(Class<?> klass)
	{
		if (klass.isEnum() || klass.isPrimitive())
		{
			return Collections.emptyList();
		}
		if (!hasInterface(klass, Serializable.class))
		{
			return Arrays.asList(klass.getCanonicalName() + " -> Not implements " + Serializable.class.getCanonicalName());
		}
		return areAllFieldsSerializable(klass);
	}
	
	/*
	 * check klass (class or interface) implements (direct or indirect - through superclasses or other interfaces) interfaceClass
	 * */
	static boolean hasInterface(Class<?> klass, Class<?> interfaceClass)
	{
		Class<?> superclass = klass;
		while (superclass != null)
		{
			if (Objects.equals(superclass, interfaceClass))
			{
				return true;
			}
			Set<Class<?>> allInterfaces = new HashSet<>();
			allInterfaces.addAll(Arrays.asList(superclass.getInterfaces()));
			int previousInterfacesCount = 0;
			int currentInterfacesCount = allInterfaces.size();
			while (currentInterfacesCount != previousInterfacesCount)
			{
				if (allInterfaces.contains(interfaceClass))
				{
					return true;
				}
				previousInterfacesCount = currentInterfacesCount;
				Set<Class<?>> itemInterfaces = new HashSet<>();
				itemInterfaces.addAll(allInterfaces);
				for (Class<?> item : allInterfaces)
				{
					itemInterfaces.addAll(Arrays.asList(item.getInterfaces()));
				}
				allInterfaces = itemInterfaces;
				currentInterfacesCount = allInterfaces.size();
			}
			if (allInterfaces.contains(interfaceClass))
			{
				return true;
			}
			superclass = superclass.getSuperclass();
		}
		return false;
	}
	
	private Predicate<Field> fieldPredicate = field -> !ModifierSupport.isStatic(field) && !Modifier.isTransient(field.getModifiers());
	
	/*
	 * check all fields of klass that they are serializable
	 * */
	private List<String> areAllFieldsSerializable(Class<?> klass)
	{
		List<Field> fields = ReflectionUtils.findFields(klass, fieldPredicate, HierarchyTraversalMode.TOP_DOWN);
		List<String> messages = new ArrayList<>();
		for (Field field : fields)
		{
			Class<?> fieldClass = field.getType();
			if (checking.contains(fieldClass))
			{
				return Collections.emptyList();
			}
			checking.add(fieldClass);
			if (fieldClass.isInterface())
			{
				if (!hasInterface(fieldClass, Serializable.class))
				{
					LOG.warn(() -> klass.getCanonicalName() + " -> " + field.getName() + " is defined with interface type " + fieldClass.getCanonicalName() + " and can contain unserializable implementation class");
				}
				if (hasInterface(fieldClass, Iterable.class)) 
				{
					processGenericArgument(field.getGenericType(), 0, messages, klass, field);
				}
				if (hasInterface(fieldClass, Map.class)) 
				{
					processGenericArgument(field.getGenericType(), 0, messages, klass, field);
					processGenericArgument(field.getGenericType(), 1, messages, klass, field);
				}
			}
			else
			{
				List<String> itemMessages = isClassSerializable(fieldClass);
				itemMessages.forEach(itemMessage-> messages.add(klass.getCanonicalName() + " -> " + field.getName() + ":" + itemMessage));
			}
			checking.remove(fieldClass);
		}
		return messages;
	}
	
	private void processGenericArgument(Type genericType, int parameterIndex, List<String> messages, Class<?> klass, Field field)
	{
		Class<?> genericArgument = getGenericArgumentClass(genericType, parameterIndex);
		List<String> itemMessages = null;
		if (genericArgument == null)
		{
			LOG.warn(() -> klass.getCanonicalName() + " -> " + field.getName() + " has undefined generic type and can contain unserializable data");
		}
		else if ((itemMessages = isClassSerializable(genericArgument)) != null && !itemMessages.isEmpty())
		{
			itemMessages.forEach(itemMessage-> messages.add(klass.getCanonicalName() + " -> " + field.getName() + ":" + itemMessage));
		}
	}
	
	/*
	 * get class of generic argument
	 * if exception acquired, returns null
	 * */
	static Class<?> getGenericArgumentClass(Type type, int parameterIndex)
	{
		int actualArgumentsCount = 0;
		String castedVariable = "type";
		try
		{
			ParameterizedType castedType = (ParameterizedType) type;
			Type[] actualArguments = castedType.getActualTypeArguments();
			actualArgumentsCount = actualArguments != null ? actualArguments.length : 0;
			Type genericArgumentType = actualArguments[parameterIndex];
			castedVariable = "genericArgumentType";
			if (genericArgumentType instanceof ParameterizedType)
			{
				return (Class<?>) ((ParameterizedType) genericArgumentType).getRawType();
			}
			else
			{
				return (Class<?>) genericArgumentType;
			}
		}
		catch (ClassCastException e)
		{
			String message = "argument '" + castedVariable + "' has type " + type.getClass().getName() + " which will be not processed";
			LOG.info(() -> message);
			return null;
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			String message = "actualArguments" + (actualArgumentsCount == 0 ? " is null" : " contains " + actualArgumentsCount + " elements") +
					" and it is impossible to get element with index " + parameterIndex;
			LOG.warn(() -> message);
			return null;
		}
	}
}