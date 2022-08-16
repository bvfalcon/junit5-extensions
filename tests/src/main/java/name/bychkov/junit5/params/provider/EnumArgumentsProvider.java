package name.bychkov.junit5.params.provider;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.NullEnum;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.platform.commons.util.Preconditions;

class EnumArgumentsProvider implements ArgumentsProvider, AnnotationConsumer<EnumSource> {

	private EnumSource enumSource;

	@Override
	public void accept(EnumSource enumSource) {
		this.enumSource = enumSource;
	}

	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
		Set<? extends Enum<?>> constants = getEnumConstants(context);
		EnumSource.Mode mode = enumSource.mode();
		String[] declaredConstantNames = enumSource.names();
		if (declaredConstantNames.length > 0) {
			Set<String> uniqueNames = stream(declaredConstantNames).collect(toSet());
			Preconditions.condition(uniqueNames.size() == declaredConstantNames.length,
				() -> "Duplicate enum constant name(s) found in " + enumSource);
			mode.validate(enumSource, constants, uniqueNames);
			constants.removeIf(constant -> !mode.select(constant, uniqueNames));
		}
		return constants.stream().map(Arguments::of);
	}

	private <E extends Enum<E>> Set<? extends E> getEnumConstants(ExtensionContext context) {
		Class<E> enumClass = determineEnumClass(context);
		return EnumSet.allOf(enumClass);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <E extends Enum<E>> Class<E> determineEnumClass(ExtensionContext context) {
		Class enumClass = enumSource.value();
		if (enumClass.equals(NullEnum.class)) {
			Class<?> klass = context.getRequiredTestClass();
			Class<?>[] parameterTypes = klass.getConstructors()[0].getParameterTypes();
			Preconditions.condition(parameterTypes.length > 0,
				() -> "Template class must declare at least one parameter: " + klass.toGenericString());
			Preconditions.condition(Enum.class.isAssignableFrom(parameterTypes[0]),
				() -> "First parameter must reference an Enum type (alternatively, use the annotation's 'value' attribute to specify the type explicitly): "
						+ klass.toGenericString());
			enumClass = parameterTypes[0];
		}
		return enumClass;
	}

}
