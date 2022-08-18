package name.bychkov.junit5;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.MemberAttributeExtension;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;

@SupportedAnnotationTypes("name.bychkov.junit5.MethodsExecution")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class MethodsExecutionAnnotationProcessor extends AbstractProcessor
{
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
	{
		processAnnotations(roundEnv, MethodsExecution.class);
		
		return true;
	}
	
	private void processAnnotations(RoundEnvironment roundEnv, Class<? extends Annotation> annotationClass)
	{
		Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotationClass);
		for (Element element : elements)
		{
			for (AnnotationMirror annotation : element.getAnnotationMirrors())
			{
				if (!MethodsExecution.class.getCanonicalName().equals(annotation.getAnnotationType().toString()))
				{
					continue;
				}
				processAnnotation(annotation, element);
			}
		}
	}
	
	static final String attribute = "value";
	
	private void processAnnotation(AnnotationMirror annotation, Element element)
	{
		System.out.println("annotation=" + annotation);
		Map<String, Object> annotationParameters = readAnnotationParameters(annotation);
		System.out.println("element=" + element);
		System.out.println("element.getClass()=" + element.getClass());
		com.sun.tools.javac.code.Symbol.ClassSymbol klass = (com.sun.tools.javac.code.Symbol.ClassSymbol) element;
		for (com.sun.tools.javac.code.Scope.Entry elem = klass.members_field.elems; elem != null; elem = elem.sibling)
		{
			System.out.println("elem.sym=" + elem.sym);
			com.sun.tools.javac.code.Symbol sym = elem.sym;
			if (sym.kind == com.sun.tools.javac.code.Kinds.MTH)
			{
				com.sun.tools.javac.code.Symbol.MethodSymbol methodSym = (com.sun.tools.javac.code.Symbol.MethodSymbol) sym;
				if ((methodSym.getAnnotation(Test.class) != null || methodSym.getAnnotation(ParameterizedTest.class) != null)
						&& methodSym.getAnnotation(Execution.class) == null)
				{
					System.out.println("is test and has no execution mode");
					System.out.println("methodSym.getSimpleName()=" + methodSym.getSimpleName());
					// TODO add annottaion
				}
			}
		}
		ExecutionMode executionMode = Optional.ofNullable(annotationParameters.get(attribute)).map(Object::toString).map(ExecutionMode::valueOf)
				.orElseThrow(() -> new RuntimeException(String.format("Annotation @%s must define the attribute %s", MethodsExecution.class.getSimpleName(), attribute)));
		System.out.println(executionMode);
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