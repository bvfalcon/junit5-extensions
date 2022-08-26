package name.bychkov.junit5.params;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;

import name.bychkov.junit5.params.ParameterizedConstructorAnnotationProcessor.ParameterizedConstructorObject;
import name.bychkov.junit5.params.provider.ValueSource;

class ValueArgumentsProvider implements ParameterizedConstructorObjectAcceptor
{
	
	private Object[] arguments;
	
	@Override
	public void accept(ParameterizedConstructorObject object)
	{
		// @formatter:off
		List<Object> arrays =
				Stream.of(
						object.valueSourceShorts,
						object.valueSourceBytes,
						object.valueSourceInts,
						object.valueSourceLongs,
						object.valueSourceFloats,
						object.valueSourceDoubles,
						object.valueSourceChars,
						object.valueSourceBooleans,
						object.valueSourceStrings,
						Stream.of(object.valueSourceClasses).map(klass -> ReflectionUtils.tryToLoadClass(klass).getOrThrow(
								cause -> new JUnitException(format("Could not load class [%s]", klass), cause))).toArray(Class<?>[]::new))
						.filter(array -> Array.getLength(array) > 0)
						.collect(toList());
		// @formatter:on
		Preconditions.condition(arrays.size() == 1, () -> "Exactly one type of input must be provided in the @"
				+ ValueSource.class.getSimpleName() + " annotation, but there were " + arrays.size());
		Object originalArray = arrays.get(0);
		arguments = IntStream.range(0, Array.getLength(originalArray)) //
				.mapToObj(index -> Array.get(originalArray, index)) //
				.toArray();
	}
	
	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context)
	{
		return Arrays.stream(arguments).map(Arguments::of);
	}
	
}
