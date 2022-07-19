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
import java.util.stream.Stream;

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

@SupportedAnnotationTypes({
		"name.bychkov.junit5.CheckConstructor",
		"name.bychkov.junit5.CheckConstructor.List",
		"name.bychkov.junit5.CheckField",
		"name.bychkov.junit5.CheckField.List",
		"name.bychkov.junit5.CheckMethod",
		"name.bychkov.junit5.CheckMethod.List" })
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
		
		// CheckKey.List and CheckKey
		processCheckAnnotations(roundEnv, CheckKey.List.class, CheckKey.class, annotationItems);
		
		// CheckResourceBundle.List and CheckResourceBundle
		processCheckAnnotations(roundEnv, CheckResourceBundle.List.class, CheckResourceBundle.class, annotationItems);
		
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
				byte[] yourBytes = bos.toByteArray();
				writer.write(yourBytes);
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
					if ("value".equals(key))
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
		else if (CheckResourceBundle.class.getCanonicalName().equals(annotation.getAnnotationType().toString()))
		{
			object = joinResourceBundle(annotation, element);
		}
		annotationItems.add(object);
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
		object.message = Optional.ofNullable(annotationParameters.get("message")).map(Object::toString).orElse(null);
		object.targetClass = getAnnotationAttribute(CheckConstructor.class, annotationParameters, "targetClass");
		object.parameters = getAnnotationArrayAttribute(annotationParameters, "parameters");
		object.annotatedElement = getAnnotatedElement(element, object.parameters);
		return object;
	}
	
	static class CheckKeyObject implements Serializable
	{
		private static final long serialVersionUID = -6028241811775586003L;
		
		String annotatedElement;
		String baseName;
		String value;
		String message;
		
		@Override
		public int hashCode()
		{
			return Objects.hash(annotatedElement, baseName, message, value);
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
			return Objects.equals(annotatedElement, other.annotatedElement) && Objects.equals(baseName, other.baseName) && Objects.equals(message, other.message) && Objects.equals(value, other.value);
		}
	}
	
	private CheckKeyObject joinKey(AnnotationMirror annotation, Element element)
	{
		Map<String, Object> annotationParameters = readAnnotationParameters(annotation);
		CheckKeyObject object = new CheckKeyObject();
		object.message = Optional.ofNullable(annotationParameters.get("message")).map(Object::toString).orElse(null);
		object.baseName = getAnnotationAttribute(CheckKey.class, annotationParameters, "baseName");
		object.value = getAnnotationAttribute(CheckKey.class, annotationParameters, "value");
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
		object.message = Optional.ofNullable(annotationParameters.get("message")).map(Object::toString).orElse(null);
		object.baseName = getAnnotationAttribute(CheckKey.class, annotationParameters, "baseName");
		object.locales = getAnnotationArrayAttribute(annotationParameters, "locales");
		object.annotatedElement = getAnnotatedElement(element, new String[0]);
		return object;
	}
	
	@SuppressWarnings("unchecked")
	private String[] getAnnotationArrayAttribute(Map<String, Object> annotationParameters, String attribute)
	{
		return Optional.ofNullable(annotationParameters.get(attribute)).map(o -> (List<AnnotationValue>) o).map(List::stream).orElseGet(Stream::empty)
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
	
	private String getAnnotationAttribute(Class<? extends Annotation> annotationClass, Map<String, Object> annotationParameters, String attribute)
	{
		return Optional.ofNullable(annotationParameters.get(attribute)).map(Object::toString)
				.orElseThrow(() -> new RuntimeException(String.format("Annotation @%s must define the attribute %s", annotationClass.getSimpleName(), attribute)));
	}
	
	private CheckFieldObject joinField(AnnotationMirror annotation, Element element)
	{
		Map<String, Object> annotationParameters = readAnnotationParameters(annotation);
		CheckFieldObject object = new CheckFieldObject();
		object.message = Optional.ofNullable(annotationParameters.get("message")).map(Object::toString).orElse(null);
		object.targetClass = getAnnotationAttribute(CheckField.class, annotationParameters, "targetClass");
		object.annotatedElement = getAnnotatedElement(element, new String[0]);
		object.value = getAnnotationAttribute(CheckField.class, annotationParameters, "value");
		object.type = Optional.ofNullable(annotationParameters.get("type")).map(Object::toString).orElse(null);
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
		object.message = Optional.ofNullable(annotationParameters.get("message")).map(Object::toString).orElse(null);
		object.targetClass = getAnnotationAttribute(CheckMethod.class, annotationParameters, "targetClass");
		object.returnType = Optional.ofNullable(annotationParameters.get("returnType")).map(Object::toString).orElse(null);
		object.parameters = getAnnotationArrayAttribute(annotationParameters, "parameters");
		object.annotatedElement = getAnnotatedElement(element, object.parameters);
		object.value = getAnnotationAttribute(CheckMethod.class, annotationParameters, "value");
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
				return element.getEnclosingElement().toString() + "." + element.getEnclosingElement().getSimpleName() + "(" + String.join(", ", parameterClassNames) + ")";
			case CLASS:
			case INTERFACE:
			case ENUM:
				return element.toString();
			default:
				return element.toString();
		}
	}
}