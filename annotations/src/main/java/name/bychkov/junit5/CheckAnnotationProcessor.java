package name.bychkov.junit5;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

@SupportedAnnotationTypes({ "name.bychkov.junit5.*" })
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class CheckAnnotationProcessor extends AbstractProcessor
{
	static final String dataFileLocation = "META-INF/maven/name.bychkov/junit5-extensions/data.dat";
	
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
	{
		Set<Serializable> annotationItems = new HashSet<>();
		
		// CheckConstructor.List and CheckConstructor
		processCheckAnnotations(roundEnv, CheckConstructor.List.class, CheckConstructor.class, annotationItems);
		
		// CheckMethod.List and CheckMethod
		processCheckAnnotations(roundEnv, CheckMethod.List.class, CheckMethod.class, annotationItems);
		
		// CheckField.List and CheckField
		processCheckAnnotations(roundEnv, CheckField.List.class, CheckField.class, annotationItems);
		
		// CheckFields.List and CheckFields
		processCheckAnnotations(roundEnv, CheckFields.List.class, CheckFields.class, annotationItems);
		
		// CheckKey.List and CheckKey
		processCheckAnnotations(roundEnv, CheckKey.List.class, CheckKey.class, annotationItems);
		
		// CheckKeys.List and CheckKeys
		processCheckAnnotations(roundEnv, CheckKeys.List.class, CheckKeys.class, annotationItems);
		
		// CheckResourceBundle.List and CheckResourceBundle
		processCheckAnnotations(roundEnv, CheckResourceBundle.List.class, CheckResourceBundle.class, annotationItems);
		
		// CheckSerializable.List and CheckSerializable
		processCheckAnnotations(roundEnv, CheckSerializable.List.class, CheckSerializable.class, annotationItems);
		
		writeFile(annotationItems);
		
		return true;
	}
	
	private void writeFile(Set<Serializable> annotationItems)
	{
		try
		{
			FileObject fileObject = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", dataFileLocation);
			try (OutputStream writer = fileObject.openOutputStream();
					ByteArrayOutputStream bos = new ByteArrayOutputStream())
			{
				ObjectOutputStream out = null;
				out = new ObjectOutputStream(bos);
				out.writeObject(annotationItems);
				out.flush();
				byte[] bytes = bos.toByteArray();
				writer.write(bytes);
			}
		}
		catch (Throwable e)
		{
			// e.printStackTrace();
		}
	}
	
	private void processCheckAnnotations(RoundEnvironment roundEnv, Class<? extends Annotation> containerAnnotationClass,
			Class<? extends Annotation> annotationClass, Collection<Serializable> annotationItems)
	{
		Set<? extends Element> containerElements = roundEnv.getElementsAnnotatedWith(containerAnnotationClass);
		for (Element element : containerElements)
		{
			for (AnnotationMirror containerAnnotation : element.getAnnotationMirrors())
			{
				for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> containerAnnotationEntry : containerAnnotation.getElementValues().entrySet())
				{
					String key = containerAnnotationEntry.getKey().getSimpleName().toString();
					if ("value".equals(key) && containerAnnotationEntry.getValue().getValue() instanceof List)
					{
						@SuppressWarnings("unchecked")
						List<Object> values = (List<Object>) containerAnnotationEntry.getValue().getValue();
						values.stream().filter(AnnotationMirror.class::isInstance).map(AnnotationMirror.class::cast)
								.forEach(annotation -> processCheckAnnotation(annotation, element, annotationItems));
					}
				}
			}
		}
		
		Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotationClass);
		for (Element element : elements)
		{
			for (AnnotationMirror annotation : element.getAnnotationMirrors())
			{
				processCheckAnnotation(annotation, element, annotationItems);
			}
		}
	}
	
	private void processCheckAnnotation(AnnotationMirror annotation, Element element, Collection<Serializable> annotationItems)
	{
		Serializable object = null;
		if (CheckField.class.getCanonicalName().equals(annotation.getAnnotationType().toString()))
		{
			object = joinField(annotation, element);
		}
		else if (CheckFields.class.getCanonicalName().equals(annotation.getAnnotationType().toString()))
		{
			object = joinFields(annotation, element);
		}
		else if (CheckMethod.class.getCanonicalName().equals(annotation.getAnnotationType().toString()))
		{
			object = joinMethod(annotation, element);
		}
		else if (CheckConstructor.class.getCanonicalName().equals(annotation.getAnnotationType().toString()))
		{
			object = joinConstructor(annotation, element);
		}
		else if (CheckKey.class.getCanonicalName().equals(annotation.getAnnotationType().toString()))
		{
			object = joinKey(annotation, element);
		}
		else if (CheckKeys.class.getCanonicalName().equals(annotation.getAnnotationType().toString()))
		{
			object = joinKeys(annotation, element);
		}
		else if (CheckResourceBundle.class.getCanonicalName().equals(annotation.getAnnotationType().toString()))
		{
			object = joinResourceBundle(annotation, element);
		}
		else if (CheckSerializable.class.getCanonicalName().equals(annotation.getAnnotationType().toString()))
		{
			object = joinSerializable(annotation, element);
		}
		
		if (object != null)
		{
			annotationItems.add(object);
		}
	}
	
	static class CheckConstructorObject implements Serializable
	{
		private static final long serialVersionUID = 3827794109493518403L;
		
		String annotatedElement;
		String targetClass;
		String[] parameters;
		String message;
		
		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(parameters);
			result = prime * result + Objects.hash(annotatedElement, message, targetClass);
			return result;
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CheckConstructorObject other = (CheckConstructorObject) obj;
			return Objects.equals(annotatedElement, other.annotatedElement) && Objects.equals(message, other.message)
					&& Arrays.equals(parameters, other.parameters) && Objects.equals(targetClass, other.targetClass);
		}
	}
	
	private Map<String, Object> readAnnotationParameters(AnnotationMirror annotation)
	{
		Map<String, Object> annotationParameters = new HashMap<>();
		for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> annotationEntry : annotation.getElementValues().entrySet())
		{
			annotationParameters.put(annotationEntry.getKey().getSimpleName().toString(), annotationEntry.getValue().getValue());
		}
		return annotationParameters;
	}
	
	private CheckConstructorObject joinConstructor(AnnotationMirror annotation, Element element)
	{
		Map<String, Object> annotationParameters = readAnnotationParameters(annotation);
		CheckConstructorObject object = new CheckConstructorObject();
		object.message = getAnnotationOptionalAttribute(annotationParameters, "message");
		object.targetClass = getAnnotationRequiredAttribute(CheckConstructor.class, annotationParameters, "targetClass");
		object.parameters = getAnnotationOptionalArrayAttribute(annotationParameters, "parameters", new String[0]);
		object.annotatedElement = getAnnotatedElement(element, object.parameters);
		return object;
	}
	
	static class CheckKeyObject implements Serializable
	{
		private static final long serialVersionUID = -4466248997083873233L;

		String annotatedElement;
		String baseName;
		String value;
		String locale;
		String message;
		
		@Override
		public int hashCode()
		{
			return Objects.hash(annotatedElement, baseName, locale, message, value);
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CheckKeyObject other = (CheckKeyObject) obj;
			return Objects.equals(annotatedElement, other.annotatedElement) && Objects.equals(baseName, other.baseName)
					&& Objects.equals(locale, other.locale) && Objects.equals(message, other.message)
					&& Objects.equals(value, other.value);
		}
	}
	
	private CheckKeyObject joinKey(AnnotationMirror annotation, Element element)
	{
		Map<String, Object> annotationParameters = readAnnotationParameters(annotation);
		CheckKeyObject object = new CheckKeyObject();
		object.message = getAnnotationOptionalAttribute(annotationParameters, "message");
		object.baseName = getAnnotationRequiredAttribute(CheckKey.class, annotationParameters, "baseName");
		object.value = getAnnotationRequiredAttribute(CheckKey.class, annotationParameters, "value");
		object.locale = getAnnotationOptionalAttribute(annotationParameters, "locale");
		object.annotatedElement = getAnnotatedElement(element, new String[0]);
		return object;
	}
	
	static class CheckKeysObject implements Serializable
	{
		private static final long serialVersionUID = 8760310613081028956L;
		
		String annotatedElement;
		String baseName;
		String[] values;
		String locale;
		String message;
		
		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(values);
			result = prime * result + Objects.hash(annotatedElement, baseName, locale, message);
			return result;
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CheckKeysObject other = (CheckKeysObject) obj;
			return Objects.equals(annotatedElement, other.annotatedElement) && Objects.equals(baseName, other.baseName)
					&& Objects.equals(locale, other.locale) && Objects.equals(message, other.message)
					&& Arrays.equals(values, other.values);
		}
	}
	
	private CheckKeysObject joinKeys(AnnotationMirror annotation, Element element)
	{
		Map<String, Object> annotationParameters = readAnnotationParameters(annotation);
		CheckKeysObject object = new CheckKeysObject();
		object.message = getAnnotationOptionalAttribute(annotationParameters, "message");
		object.baseName = getAnnotationRequiredAttribute(CheckKeys.class, annotationParameters, "baseName");
		object.values = getAnnotationRequiredArrayAttribute(CheckKeys.class, annotationParameters, "values");
		object.locale = getAnnotationOptionalAttribute(annotationParameters, "locale");
		object.annotatedElement = getAnnotatedElement(element, new String[0]);
		return object;
	}
	
	static class CheckSerializableObject implements Serializable
	{
		private static final long serialVersionUID = -6824914762264876265L;
		
		String annotatedElement;
		String targetPackage;
		String[] excludes;
		String message;
		
		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(excludes);
			result = prime * result + Objects.hash(annotatedElement, message, targetPackage);
			return result;
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CheckSerializableObject other = (CheckSerializableObject) obj;
			return Objects.equals(annotatedElement, other.annotatedElement) && Arrays.equals(excludes, other.excludes)
					&& Objects.equals(message, other.message) && Objects.equals(targetPackage, other.targetPackage);
		}
	}
	
	private CheckSerializableObject joinSerializable(AnnotationMirror annotation, Element element)
	{
		Map<String, Object> annotationParameters = readAnnotationParameters(annotation);
		CheckSerializableObject object = new CheckSerializableObject();
		object.message = getAnnotationOptionalAttribute(annotationParameters, "message");
		object.targetPackage = getAnnotationOptionalAttribute(annotationParameters, "targetPackage");
		object.excludes = getAnnotationOptionalArrayAttribute(annotationParameters, "excludes", new String[0]);
		object.annotatedElement = getAnnotatedElement(element, new String[0]);
		return object;
	}
	
	static class CheckResourceBundleObject implements Serializable
	{
		private static final long serialVersionUID = -6200874311724044569L;
		
		String annotatedElement;
		String baseName;
		String[] locales;
		String message;
		
		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(locales);
			result = prime * result + Objects.hash(annotatedElement, baseName, message);
			return result;
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CheckResourceBundleObject other = (CheckResourceBundleObject) obj;
			return Objects.equals(annotatedElement, other.annotatedElement) && Objects.equals(baseName, other.baseName) && Arrays.equals(locales, other.locales) && Objects.equals(message, other.message);
		}
	}
	
	private CheckResourceBundleObject joinResourceBundle(AnnotationMirror annotation, Element element)
	{
		Map<String, Object> annotationParameters = readAnnotationParameters(annotation);
		CheckResourceBundleObject object = new CheckResourceBundleObject();
		object.message = getAnnotationOptionalAttribute(annotationParameters, "message");
		object.baseName = getAnnotationRequiredAttribute(CheckResourceBundle.class, annotationParameters, "baseName");
		object.locales = getAnnotationRequiredArrayAttribute(CheckResourceBundle.class, annotationParameters, "locales");
		object.annotatedElement = getAnnotatedElement(element, new String[0]);
		return object;
	}
	
	@SuppressWarnings("unchecked")
	private String[] getAnnotationOptionalArrayAttribute(Map<String, Object> annotationParameters, String attribute, String[] defaultValue)
	{
		List<AnnotationValue> list = (List<AnnotationValue>) annotationParameters.get(attribute);
		if (list == null)
		{
			return defaultValue;
		}
		return list.stream().map(AnnotationValue::getValue).map(Object::toString).toArray(String[]::new);
	}
	
	@SuppressWarnings("unchecked")
	private String[] getAnnotationRequiredArrayAttribute(Class<? extends Annotation> annotationClass, Map<String, Object> annotationParameters, String attribute)
	{
		return Optional.ofNullable(annotationParameters.get(attribute)).map(o -> (List<AnnotationValue>) o).map(List::stream)
				.orElseThrow(() -> new RuntimeException(String.format("Annotation @%s must define the attribute %s", annotationClass.getSimpleName(), attribute)))
				.map(AnnotationValue::getValue).map(Object::toString).toArray(String[]::new);
	}
	
	static class CheckFieldObject implements Serializable
	{
		private static final long serialVersionUID = 223641553109037040L;
		
		String annotatedElement;
		String targetClass;
		String value;
		String type;
		String message;
		
		@Override
		public int hashCode()
		{
			return Objects.hash(annotatedElement, message, targetClass, type, value);
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CheckFieldObject other = (CheckFieldObject) obj;
			return Objects.equals(annotatedElement, other.annotatedElement) && Objects.equals(message, other.message)
					&& Objects.equals(targetClass, other.targetClass) && Objects.equals(type, other.type) && Objects.equals(value, other.value);
		}
	}
	
	private String getAnnotationRequiredAttribute(Class<? extends Annotation> annotationClass, Map<String, Object> annotationParameters, String attribute)
	{
		return Optional.ofNullable(annotationParameters.get(attribute)).map(Object::toString)
				.orElseThrow(() -> new RuntimeException(String.format("Annotation @%s must define the attribute %s", annotationClass.getSimpleName(), attribute)));
	}
	
	private String getAnnotationOptionalAttribute(Map<String, Object> annotationParameters, String attribute)
	{
		return Optional.ofNullable(annotationParameters.get(attribute)).map(Object::toString).orElse(null);
	}
	
	private CheckFieldObject joinField(AnnotationMirror annotation, Element element)
	{
		Map<String, Object> annotationParameters = readAnnotationParameters(annotation);
		CheckFieldObject object = new CheckFieldObject();
		object.message = getAnnotationOptionalAttribute(annotationParameters, "message");
		object.targetClass = getAnnotationRequiredAttribute(CheckField.class, annotationParameters, "targetClass");
		object.annotatedElement = getAnnotatedElement(element, new String[0]);
		object.value = getAnnotationRequiredAttribute(CheckField.class, annotationParameters, "value");
		object.type = getAnnotationOptionalAttribute(annotationParameters, "type");
		return object;
	}
	
	static class CheckFieldsObject implements Serializable
	{
		private static final long serialVersionUID = 1586354720019392961L;
		
		String annotatedElement;
		String targetClass;
		String[] values;
		String message;
		
		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(values);
			result = prime * result + Objects.hash(annotatedElement, message, targetClass);
			return result;
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CheckFieldsObject other = (CheckFieldsObject) obj;
			return Objects.equals(annotatedElement, other.annotatedElement) && Objects.equals(message, other.message)
					&& Objects.equals(targetClass, other.targetClass) && Arrays.equals(values, other.values);
		}
	}
	
	private CheckFieldsObject joinFields(AnnotationMirror annotation, Element element)
	{
		Map<String, Object> annotationParameters = readAnnotationParameters(annotation);
		CheckFieldsObject object = new CheckFieldsObject();
		object.message = getAnnotationOptionalAttribute(annotationParameters, "message");
		object.targetClass = getAnnotationRequiredAttribute(CheckFields.class, annotationParameters, "targetClass");
		object.annotatedElement = getAnnotatedElement(element, new String[0]);
		object.values = getAnnotationRequiredArrayAttribute(CheckFields.class, annotationParameters, "values");
		return object;
	}
	
	static class CheckMethodObject implements Serializable
	{
		private static final long serialVersionUID = -7087204752344865404L;
		
		String annotatedElement;
		String targetClass;
		String returnType;
		String value;
		String[] parameters;
		String message;
		
		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(parameters);
			result = prime * result + Objects.hash(annotatedElement, message, returnType, targetClass, value);
			return result;
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CheckMethodObject other = (CheckMethodObject) obj;
			return Objects.equals(annotatedElement, other.annotatedElement) && Objects.equals(message, other.message)
					&& Arrays.equals(parameters, other.parameters) && Objects.equals(returnType, other.returnType)
					&& Objects.equals(targetClass, other.targetClass) && Objects.equals(value, other.value);
		}
	}
	
	private CheckMethodObject joinMethod(AnnotationMirror annotation, Element element)
	{
		Map<String, Object> annotationParameters = readAnnotationParameters(annotation);
		CheckMethodObject object = new CheckMethodObject();
		object.message = getAnnotationOptionalAttribute(annotationParameters, "message");
		object.targetClass = getAnnotationRequiredAttribute(CheckMethod.class, annotationParameters, "targetClass");
		object.returnType = getAnnotationOptionalAttribute(annotationParameters, "returnType");
		object.parameters = getAnnotationOptionalArrayAttribute(annotationParameters, "parameters", null);
		object.annotatedElement = getAnnotatedElement(element, object.parameters);
		object.value = getAnnotationRequiredAttribute(CheckMethod.class, annotationParameters, "value");
		return object;
	}
	
	private String getAnnotatedElement(Element element, String[] parameterClassNames)
	{
		switch (element.getKind())
		{
			case METHOD:
				return element.getEnclosingElement().toString() + "." + element.toString();
			case FIELD:
			case ENUM_CONSTANT:
				return element.getEnclosingElement().toString() + "." + element.toString();
			case CONSTRUCTOR:
				return element.getEnclosingElement().toString() + "." + 
					element.getEnclosingElement().getSimpleName() +
					(parameterClassNames != null ? ("(" + String.join(", ", parameterClassNames) + ")") : "");
			case CLASS:
			case INTERFACE:
			case ENUM:
			case PACKAGE:
				return element.toString();
			default:
				return element.toString();
		}
	}
}