package name.bychkov.junit5.params;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;

import java.lang.reflect.Constructor;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;

import name.bychkov.junit5.params.ParameterizedConstructorAnnotationProcessor.ParameterizedConstructorObject;
import name.bychkov.junit5.params.provider.EnumSource.Mode;

class EnumArgumentsProvider implements ParameterizedConstructorObjectAcceptor {

	private String value;
	private String[] names;
	private Mode mode;

	@Override
	public void accept(ParameterizedConstructorObject object) {
		this.value = object.enumSourceValue;
		this.names = object.enumSourceNames;
		this.mode = object.enumSourceMode != null ? Mode.valueOf(object.enumSourceMode) : Mode.INCLUDE;
	}

	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
		Set<? extends Enum<?>> constants = getEnumConstants(context);
		String[] declaredConstantNames = names;
		if (declaredConstantNames.length > 0) {
			Set<String> uniqueNames = stream(declaredConstantNames).collect(toSet());
			Preconditions.condition(uniqueNames.size() == declaredConstantNames.length,
				() -> "Duplicate enum constant name(s) found in @EnumSource");
			mode.validate(constants, uniqueNames);
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
		if (value == null) {
			Constructor constructor = (Constructor) Preconditions.notNull(context.getElement().orElse(null),
					"Illegal state: required test constructor is not present in the current ExtensionContext");
			Class<?>[] parameterTypes = constructor.getParameterTypes();
			Preconditions.condition(parameterTypes.length > 0,
				() -> "Test constructor must declare at least one parameter: " + constructor.toGenericString());
			Preconditions.condition(Enum.class.isAssignableFrom(parameterTypes[0]),
				() -> "First parameter must reference an Enum type (alternatively, use the annotation's 'value' attribute to specify the type explicitly): "
						+ constructor.toGenericString());
			return (Class<E>) parameterTypes[0];
		}
		else {
			return (Class<E>) ReflectionUtils.tryToLoadClass(value).getOrThrow(
					cause -> new JUnitException(format("Could not load class [%s]", value), cause));
		}
	}

}
