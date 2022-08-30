package name.bychkov.junit5.params.provider;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.lang.reflect.Constructor;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.platform.commons.util.Preconditions;

class NullArgumentsProvider implements ArgumentsProvider {

	private static final Arguments nullArguments = arguments(new Object[] { null });

	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
		Constructor<?> constructor = (Constructor<?>) context.getElement().get();
		Preconditions.condition(constructor.getParameterCount() > 0, () -> String.format(
			"@NullSource cannot provide a null argument to constructor [%s]: the method does not declare any formal parameters.",
			constructor.toGenericString()));

		return Stream.of(nullArguments);
	}

}
