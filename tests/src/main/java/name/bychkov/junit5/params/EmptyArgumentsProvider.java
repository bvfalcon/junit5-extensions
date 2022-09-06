package name.bychkov.junit5.params;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.util.Preconditions;

import name.bychkov.junit5.params.ParameterizedConstructorAnnotationProcessor.ParameterizedConstructorObject;

class EmptyArgumentsProvider implements ParameterizedConstructorObjectAcceptor {

	@Override
	public void accept(ParameterizedConstructorObject object) {
	}
	
	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
		Constructor<?> constructor = (Constructor<?>) context.getElement().get();
		Class<?>[] parameterTypes = constructor.getParameterTypes();

		Preconditions.condition(parameterTypes.length > 0, () -> String.format(
			"@EmptySource cannot provide an empty argument to constructor [%s]: the method does not declare any formal parameters.",
			constructor.toGenericString()));

		Class<?> parameterType = parameterTypes[0];

		if (String.class.equals(parameterType)) {
			return Stream.of(arguments(""));
		}
		if (List.class.equals(parameterType)) {
			return Stream.of(arguments(Collections.emptyList()));
		}
		if (Set.class.equals(parameterType)) {
			return Stream.of(arguments(Collections.emptySet()));
		}
		if (Map.class.equals(parameterType)) {
			return Stream.of(arguments(Collections.emptyMap()));
		}
		if (parameterType.isArray()) {
			Object array = Array.newInstance(parameterType.getComponentType(), 0);
			return Stream.of(arguments(array));
		}
		// else
		throw new PreconditionViolationException(
			String.format("@EmptySource cannot provide an empty argument to constructor [%s]: [%s] is not a supported type.",
					constructor.toGenericString(), parameterType.getName()));
	}
}
