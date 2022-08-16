package name.bychkov.junit5.params.provider;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.util.Preconditions;

class EmptyArgumentsProvider implements ArgumentsProvider {

	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
		Class<?> testClass = context.getRequiredTestClass();
		Class<?>[] parameterTypes = testClass.getConstructors()[0].getParameterTypes();

		Preconditions.condition(parameterTypes.length > 0, () -> String.format(
			"@EmptySource cannot provide an empty argument to class [%s]: the class constructor does not declare any formal parameters.",
			testClass.toGenericString()));

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
			String.format("@EmptySource cannot provide an empty argument to class [%s]: [%s] is not a supported type.",
				testClass.toGenericString(), parameterType.getName()));
	}

}
