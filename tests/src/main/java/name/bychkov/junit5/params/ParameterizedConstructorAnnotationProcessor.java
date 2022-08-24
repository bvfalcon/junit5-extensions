package name.bychkov.junit5.params;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

@SupportedAnnotationTypes("name.bychkov.junit5.params.ParameterizedConstructor")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ParameterizedConstructorAnnotationProcessor extends AbstractProcessor
{
	static final String DATA_FILE_LOCATION = "META-INF/maven/name.bychkov/junit5-extensions/parameterized-constructor-data.dat";
	
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
	{
		Set<Serializable> annotationItems = new HashSet<>();
		processAnnotations(roundEnv, ParameterizedConstructor.class, annotationItems);
		writeFile(DATA_FILE_LOCATION, annotationItems);
		return true;
	}
	
	private void processAnnotations(RoundEnvironment roundEnv, Class<? extends Annotation> annotationClass, Collection<Serializable> annotationItems)
	{
		Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotationClass);
		for (Element element : elements)
		{
			for (AnnotationMirror annotation : element.getAnnotationMirrors())
			{
				if (!annotationClass.getCanonicalName().equals(annotation.getAnnotationType().toString()))
				{
					continue;
				}
				processAnnotation(annotation, element, annotationItems);
			}
		}
	}
	
	private void processAnnotation(AnnotationMirror annotation, Element element, Collection<Serializable> annotationItems)
	{
		Serializable object = null;
		if (ParameterizedConstructor.class.getCanonicalName().equals(annotation.getAnnotationType().toString()))
		{
			object = join(annotation, element);
		}
		if (object != null)
		{
			annotationItems.add(object);
		}
	}
	
	static class ParameterizedConstructorObject implements Serializable
	{
		private static final long serialVersionUID = -6440198292964159222L;
		
		String annotatedElement;
		String targetClass;
		String[] parameters;
		
		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(parameters);
			result = prime * result + Objects.hash(annotatedElement, targetClass);
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
			return Objects.equals(annotatedElement, other.annotatedElement)
					&& Arrays.equals(parameters, other.parameters) && Objects.equals(targetClass, other.targetClass);
		}
	}
	
	private ParameterizedConstructorObject join(AnnotationMirror annotation, Element element)
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
			String type = parameter.asType().toString();
			parameterTypes.add(type);
		}
		object.parameters = parameterTypes.toArray(new String[0]);
		object.annotatedElement = object.targetClass + "." + element.getEnclosingElement().getSimpleName() + ("(" + String.join(", ", object.parameters) + ")");
		return object;
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
		catch (Throwable e)
		{
			// e.printStackTrace();
		}
	}
}