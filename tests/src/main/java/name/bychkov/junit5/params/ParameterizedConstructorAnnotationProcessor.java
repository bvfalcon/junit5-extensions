package name.bychkov.junit5.params;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import name.bychkov.junit5.params.provider.EmptySource;
import name.bychkov.junit5.params.provider.EnumSource;
import name.bychkov.junit5.params.provider.MethodSource;
import name.bychkov.junit5.params.provider.NullSource;
import name.bychkov.junit5.params.provider.ValueSource;

@SupportedAnnotationTypes("name.bychkov.junit5.params.ParameterizedConstructor")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ParameterizedConstructorAnnotationProcessor extends AbstractProcessor
{
	private static final Logger LOG = Logger.getLogger(ParameterizedConstructorAnnotationProcessor.class.getSimpleName());
	static final String DATA_FILE_LOCATION = "META-INF/maven/name.bychkov/junit5-extensions/parameterized-constructor-data.dat";
	
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
	{
		try
		{
			Set<Serializable> annotationItems = new HashSet<>();
			processAnnotations(roundEnv, ParameterizedConstructor.class,
					Arrays.asList(EmptySource.class, EnumSource.class, MethodSource.class, NullSource.class, ValueSource.class),
					annotationItems);
			writeFile(DATA_FILE_LOCATION, annotationItems);
		}
		catch (Exception e)
		{
			LOG.log(Level.SEVERE, e, () -> "Error has acquired while @ParameterizedConstructor annotation processing");
		}
		return true;
	}
	
	private void processAnnotations(RoundEnvironment roundEnv, Class<? extends Annotation> mainAnnotationClass,
			List<Class<? extends Annotation>> dataSourceAnnotationClasses, Collection<Serializable> annotationItems)
	{
		Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(mainAnnotationClass);
		for (Element element : elements)
		{
			List<AnnotationMirror> dataSourceAnnotationMirrors = new ArrayList<>();
			for (AnnotationMirror annotation : element.getAnnotationMirrors())
			{
				String annotationCanonicalName = annotation.getAnnotationType().toString();
				if (dataSourceAnnotationClasses.stream().map(Class::getCanonicalName).anyMatch(a -> Objects.equals(a, annotationCanonicalName)))
				{
					dataSourceAnnotationMirrors.add(annotation);
				}
			}
			processAnnotation(dataSourceAnnotationMirrors, element, annotationItems);
		}
	}
	
	private void processAnnotation(List<AnnotationMirror> dataSourceAnnotations, Element element, Collection<Serializable> annotationItems)
	{
		Serializable object = join(element, dataSourceAnnotations);
		annotationItems.add(object);
	}
	
	static class ParameterizedConstructorObject implements Serializable
	{
		private static final long serialVersionUID = 5216537137179260702L;
		
		String annotatedElement;
		String targetClass;
		String[] parameters;
		
		boolean hasEmptySource;
		boolean hasNullSource;
		
		boolean hasEnumSource;
		String enumSourceValue;
		String[] enumSourceNames;
		String enumSourceMode;
		
		boolean hasValueSource;
		short[] valueSourceShorts;
		byte[] valueSourceBytes;
		int[] valueSourceInts;
		long[] valueSourceLongs;
		float[] valueSourceFloats;
		double[] valueSourceDoubles;
		char[] valueSourceChars;
		boolean[] valueSourceBooleans;
		String[] valueSourceStrings;
		String[] valueSourceClasses;
		
		boolean hasMethodSource;
		String[] methodSourceValue;
		
		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(enumSourceNames);
			result = prime * result + Arrays.hashCode(methodSourceValue);
			result = prime * result + Arrays.hashCode(parameters);
			result = prime * result + Arrays.hashCode(valueSourceBooleans);
			result = prime * result + Arrays.hashCode(valueSourceBytes);
			result = prime * result + Arrays.hashCode(valueSourceChars);
			result = prime * result + Arrays.hashCode(valueSourceClasses);
			result = prime * result + Arrays.hashCode(valueSourceDoubles);
			result = prime * result + Arrays.hashCode(valueSourceFloats);
			result = prime * result + Arrays.hashCode(valueSourceInts);
			result = prime * result + Arrays.hashCode(valueSourceLongs);
			result = prime * result + Arrays.hashCode(valueSourceShorts);
			result = prime * result + Arrays.hashCode(valueSourceStrings);
			result = prime * result + Objects.hash(annotatedElement, enumSourceMode, enumSourceValue,
					hasEmptySource, hasEnumSource, hasMethodSource, hasNullSource, hasValueSource, targetClass);
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
			ParameterizedConstructorObject other = (ParameterizedConstructorObject) obj;
			return Objects.equals(annotatedElement, other.annotatedElement) && enumSourceMode == other.enumSourceMode
					&& Arrays.equals(enumSourceNames, other.enumSourceNames) && Objects.equals(enumSourceValue, other.enumSourceValue)
					&& hasEmptySource == other.hasEmptySource && hasEnumSource == other.hasEnumSource
					&& hasMethodSource == other.hasMethodSource && hasNullSource == other.hasNullSource
					&& hasValueSource == other.hasValueSource && Arrays.equals(methodSourceValue, other.methodSourceValue)
					&& Arrays.equals(parameters, other.parameters) && Objects.equals(targetClass, other.targetClass)
					&& Arrays.equals(valueSourceBooleans, other.valueSourceBooleans) && Arrays.equals(valueSourceBytes, other.valueSourceBytes)
					&& Arrays.equals(valueSourceChars, other.valueSourceChars) && Arrays.equals(valueSourceClasses, other.valueSourceClasses)
					&& Arrays.equals(valueSourceDoubles, other.valueSourceDoubles) && Arrays.equals(valueSourceFloats, other.valueSourceFloats)
					&& Arrays.equals(valueSourceInts, other.valueSourceInts) && Arrays.equals(valueSourceLongs, other.valueSourceLongs)
					&& Arrays.equals(valueSourceShorts, other.valueSourceShorts) && Arrays.equals(valueSourceStrings, other.valueSourceStrings);
		}
	}
	
	private String getType(com.sun.tools.javac.code.Symbol.VarSymbol parameter)
	{
		String type = ((com.sun.tools.javac.code.Type) parameter.asType()).tsym.toString();
		TypeElement typeElement = processingEnv.getElementUtils().getTypeElement(type);
		if (typeElement != null)
		{
			type = processingEnv.getElementUtils().getBinaryName(typeElement).toString();
		}
		return type;
	}
	
	private ParameterizedConstructorObject join(Element element, List<AnnotationMirror> dataSourceAnnotations)
	{
		ParameterizedConstructorObject object = new ParameterizedConstructorObject();
		if (element.getEnclosingElement().getKind() == ElementKind.CLASS)
		{
			object.targetClass = element.getEnclosingElement().toString();
		}
		List<com.sun.tools.javac.code.Symbol.VarSymbol> parameters = ((com.sun.tools.javac.code.Symbol.MethodSymbol) element).getParameters();
		List<String> parameterTypes = new ArrayList<>(parameters.size());
		for (com.sun.tools.javac.code.Symbol.VarSymbol parameter : parameters)
		{
			String type = getType(parameter);
			parameterTypes.add(type);
		}
		object.parameters = parameterTypes.toArray(new String[0]);
		object.annotatedElement = object.targetClass + "." + element.getEnclosingElement().getSimpleName() + ("(" + String.join(", ", object.parameters) + ")");
		object.hasEmptySource = getAnnotation(dataSourceAnnotations, EmptySource.class) != null;
		object.hasNullSource = getAnnotation(dataSourceAnnotations, NullSource.class) != null;
		
		AnnotationMirror enumMirror = getAnnotation(dataSourceAnnotations, EnumSource.class);
		object.hasEnumSource = enumMirror != null;
		if (object.hasEnumSource)
		{
			Map<String, Object> annotationParameters = readAnnotationParameters(enumMirror);
			object.enumSourceValue = getAnnotationOptionalAttribute(annotationParameters, "value");
			object.enumSourceNames = getAnnotationOptionalArrayAttribute(annotationParameters, "names", new String[0]);
			object.enumSourceMode = getAnnotationOptionalAttribute(annotationParameters, "mode");
		}
		
		AnnotationMirror valueMirror = getAnnotation(dataSourceAnnotations, ValueSource.class);
		object.hasValueSource = valueMirror != null;
		if (object.hasValueSource)
		{
			Map<String, Object> annotationParameters = readAnnotationParameters(valueMirror);
			object.valueSourceShorts = toArrayOfShort(getAnnotationOptionalArrayAttribute(annotationParameters, "shorts", new String[0]));
			object.valueSourceBytes = toArrayOfByte(getAnnotationOptionalArrayAttribute(annotationParameters, "bytes", new String[0]));
			object.valueSourceInts = Stream.of(getAnnotationOptionalArrayAttribute(annotationParameters, "ints", new String[0])).mapToInt(Integer::valueOf).toArray();
			object.valueSourceLongs = Stream.of(getAnnotationOptionalArrayAttribute(annotationParameters, "longs", new String[0])).mapToLong(Long::valueOf).toArray();
			object.valueSourceFloats = toArrayOfFloat(getAnnotationOptionalArrayAttribute(annotationParameters, "floats", new String[0]));
			object.valueSourceDoubles = Stream.of(getAnnotationOptionalArrayAttribute(annotationParameters, "doubles", new String[0])).mapToDouble(Double::valueOf).toArray();
			object.valueSourceChars = toArrayOfChar(getAnnotationOptionalArrayAttribute(annotationParameters, "chars", new String[0]));
			object.valueSourceBooleans = toArrayOfBoolean(getAnnotationOptionalArrayAttribute(annotationParameters, "booleans", new String[0]));
			object.valueSourceStrings = getAnnotationOptionalArrayAttribute(annotationParameters, "strings", new String[0]);
			object.valueSourceClasses = getAnnotationOptionalArrayAttribute(annotationParameters, "classes", new String[0]);
		}
		
		AnnotationMirror methodMirror = getAnnotation(dataSourceAnnotations, MethodSource.class);
		object.hasMethodSource = methodMirror != null;
		if (object.hasMethodSource)
		{
			Map<String, Object> annotationParameters = readAnnotationParameters(methodMirror);
			object.methodSourceValue = getAnnotationOptionalArrayAttribute(annotationParameters, "value", new String[0]);
		}
		return object;
	}
	
	private short[] toArrayOfShort(String[] source)
	{
		short[] result = new short[source.length];
		for (int i = 0; i < source.length; i++)
		{
			result[i] = Short.parseShort(source[i]);
		}
		return result;
	}
	
	private boolean[] toArrayOfBoolean(String[] source)
	{
		boolean[] result = new boolean[source.length];
		for (int i = 0; i < source.length; i++)
		{
			result[i] = Boolean.parseBoolean(source[i]);
		}
		return result;
	}
	
	private char[] toArrayOfChar(String[] source)
	{
		char[] result = new char[source.length];
		for (int i = 0; i < source.length; i++)
		{
			result[i] = source[i].charAt(0);
		}
		return result;
	}
	
	private byte[] toArrayOfByte(String[] source)
	{
		byte[] result = new byte[source.length];
		for (int i = 0; i < source.length; i++)
		{
			result[i] = Byte.parseByte(source[i]);
		}
		return result;
	}
	
	private float[] toArrayOfFloat(String[] source)
	{
		float[] result = new float[source.length];
		for (int i = 0; i < source.length; i++)
		{
			result[i] = Float.parseFloat(source[i]);
		}
		return result;
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
	
	private String getAnnotationOptionalAttribute(Map<String, Object> annotationParameters, String attribute)
	{
		return Optional.ofNullable(annotationParameters.get(attribute)).map(Object::toString).orElse(null);
	}
	
	private AnnotationMirror getAnnotation(List<AnnotationMirror> mirrorAnnotations, Class<? extends Annotation> annotation)
	{
		return mirrorAnnotations.stream().filter(a -> Objects.equals(a.getAnnotationType().toString(), annotation.getCanonicalName())).findFirst().orElse(null);
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
	
	private void writeFile(String filename, Set<Serializable> annotationItems)
	{
		try
		{
			FileObject fileObject = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", filename);
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
		catch (javax.annotation.processing.FilerException e)
		{
			LOG.log(Level.FINER, e, () -> "File " + filename + " already exists. Rewriting file is impossible");
		}
		catch (Exception e)
		{
			LOG.log(Level.SEVERE, e, () -> "Error has acquired while File " + filename + " writing");
		}
	}
}