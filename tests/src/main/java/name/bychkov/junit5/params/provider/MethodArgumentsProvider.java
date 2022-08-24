package name.bychkov.junit5.params.provider;

import static java.lang.String.format;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.CollectionUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.StringUtils;

class MethodArgumentsProvider implements ArgumentsProvider, AnnotationConsumer<MethodSource> {

	private String[] methodNames;

	@Override
	public void accept(MethodSource annotation) {
		this.methodNames = annotation.value();
	}

	@Override
	public Stream<Arguments> provideArguments(ExtensionContext context) {
		// @formatter:off
		return Arrays.stream(this.methodNames)
				.map(factoryMethodName -> getFactoryMethod(context, factoryMethodName))
				.map(factoryMethod -> ReflectionUtils.invokeMethod(factoryMethod, null))
				.flatMap(CollectionUtils::toStream)
				.map(MethodArgumentsProvider::toArguments);
		// @formatter:on
	}

	private Method getFactoryMethod(ExtensionContext context, String factoryMethodName) {
		Constructor<?> constructor = (Constructor<?>) context.getElement().get();
		if (StringUtils.isBlank(factoryMethodName)) {
			throw new JUnitException(format("Factory method for %s on constructor %s(%s) must be not empty", MethodSource.class.getSimpleName(),
					constructor.getName(), Stream.of(constructor.getParameterTypes()).map(Class::getName).collect(Collectors.joining(", "))));
		}
		if (factoryMethodName.contains(".") || factoryMethodName.contains("#")) {
			return getFactoryMethodByFullyQualifiedName(factoryMethodName);
		}
		return getFactoryMethodBySimpleName(context.getRequiredTestClass(), factoryMethodName);
	}

	private Method getFactoryMethodByFullyQualifiedName(String fullyQualifiedMethodName) {
		String[] methodParts = ReflectionUtils.parseFullyQualifiedMethodName(fullyQualifiedMethodName);
		String className = methodParts[0];
		String methodName = methodParts[1];
		String methodParameters = methodParts[2];

		Method result = ReflectionUtils.findMethod(loadRequiredClass(className), methodName, methodParameters).orElseThrow(
			() -> new JUnitException(format("Could not find factory method [%s(%s)] in class [%s]", methodName,
				methodParameters, className)));
		Preconditions.condition(ReflectionUtils.isStatic(result),
				() -> format("Factory method [%s] in class [%s] must be static", methodName, className));
		return result;
	}

	private Method getFactoryMethodBySimpleName(Class<?> testClass, String factoryMethodName) {
		// Find all methods with the desired factory method name, but ignore the test method itself.
		List<Method> methods = ReflectionUtils.findMethods(testClass,
			factoryMethod -> factoryMethodName.equals(factoryMethod.getName()) && ReflectionUtils.isStatic(factoryMethod));
		Preconditions.condition(methods.size() > 0,
			() -> format("Could not find static factory method [%s] in class [%s]", factoryMethodName, testClass.getName()));
		Preconditions.condition(methods.size() == 1,
			() -> format("Several factory methods named [%s] were found in class [%s]", factoryMethodName,
				testClass.getName()));
		return methods.get(0);
	}

	private Class<?> loadRequiredClass(String className) {
		return ReflectionUtils.tryToLoadClass(className).getOrThrow(
			cause -> new JUnitException(format("Could not load class [%s]", className), cause));
	}

	private static Arguments toArguments(Object item) {
		// Nothing to do except cast.
		if (item instanceof Arguments) {
			return (Arguments) item;
		}

		// Pass all multidimensional arrays "as is", in contrast to Object[].
		// See https://github.com/junit-team/junit5/issues/1665
		if (ReflectionUtils.isMultidimensionalArray(item)) {
			return arguments(item);
		}

		// Special treatment for one-dimensional reference arrays.
		// See https://github.com/junit-team/junit5/issues/1665
		if (item instanceof Object[]) {
			return arguments((Object[]) item);
		}

		// Pass everything else "as is".
		return arguments(item);
	}

}
