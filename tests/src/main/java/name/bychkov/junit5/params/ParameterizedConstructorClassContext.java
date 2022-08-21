package name.bychkov.junit5.params;

import static org.junit.platform.commons.util.AnnotationUtils.isAnnotated;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;
import org.junit.jupiter.params.aggregator.DefaultArgumentsAccessor;
import org.junit.jupiter.params.converter.ArgumentConverter;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.converter.DefaultArgumentConverter;
import org.junit.jupiter.params.support.AnnotationConsumerInitializer;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.StringUtils;

class ParameterizedConstructorClassContext {

	private final Parameter[] parameters;
	private final Resolver[] resolvers;
	private final List<ResolverType> resolverTypes;

	ParameterizedConstructorClassContext(Class<?> testClass) {
		this.parameters = testClass.getConstructors()[0].getParameters();
		this.resolvers = new Resolver[this.parameters.length];
		this.resolverTypes = new ArrayList<>(this.parameters.length);
		for (Parameter parameter : this.parameters) {
			this.resolverTypes.add(isAggregator(parameter) ? AGGREGATOR : CONVERTER);
		}
	}

	private static boolean isAggregator(Parameter parameter) {
		return ArgumentsAccessor.class.isAssignableFrom(parameter.getType())
				|| isAnnotated(parameter, AggregateWith.class);
	}

	boolean hasPotentiallyValidSignature() {
		int indexOfPreviousAggregator = -1;
		for (int i = 0; i < getParameterCount(); i++) {
			if (isAggregator(i)) {
				if ((indexOfPreviousAggregator != -1) && (i != indexOfPreviousAggregator + 1)) {
					return false;
				}
				indexOfPreviousAggregator = i;
			}
		}
		return true;
	}

	int getParameterCount() {
		return parameters.length;
	}

	Optional<String> getParameterName(int parameterIndex) {
		if (parameterIndex >= getParameterCount()) {
			return Optional.empty();
		}
		Parameter parameter = this.parameters[parameterIndex];
		if (!parameter.isNamePresent()) {
			return Optional.empty();
		}
		if (hasAggregator() && parameterIndex >= indexOfFirstAggregator()) {
			return Optional.empty();
		}
		return Optional.of(parameter.getName());
	}


	boolean hasAggregator() {
		return resolverTypes.contains(AGGREGATOR);
	}

	boolean isAggregator(int parameterIndex) {
		return resolverTypes.get(parameterIndex) == AGGREGATOR;
	}

	int indexOfFirstAggregator() {
		return resolverTypes.indexOf(AGGREGATOR);
	}

	Object resolve(ParameterContext parameterContext, Object[] arguments) {
		return getResolver(parameterContext).resolve(parameterContext, arguments);
	}

	private Resolver getResolver(ParameterContext parameterContext) {
		int index = parameterContext.getIndex();
		if (resolvers[index] == null) {
			resolvers[index] = resolverTypes.get(index).createResolver(parameterContext);
		}
		return resolvers[index];
	}

	enum ResolverType {

		CONVERTER {
			@Override
			Resolver createResolver(ParameterContext parameterContext) {
				try { // @formatter:off
					return AnnotationUtils.findAnnotation(parameterContext.getParameter(), ConvertWith.class)
							.map(ConvertWith::value)
							.map(clazz -> (ArgumentConverter) ReflectionUtils.newInstance(clazz))
							.map(converter -> AnnotationConsumerInitializer.initialize(parameterContext.getParameter(), converter))
							.map(Converter::new)
							.orElse(Converter.DEFAULT);
				} // @formatter:on
				catch (Exception ex) {
					throw parameterResolutionException("Error creating ArgumentConverter", ex, parameterContext);
				}
			}
		},

		AGGREGATOR {
			@Override
			Resolver createResolver(ParameterContext parameterContext) {
				try { // @formatter:off
					return AnnotationUtils.findAnnotation(parameterContext.getParameter(), AggregateWith.class)
							.map(AggregateWith::value)
							.map(clazz -> (ArgumentsAggregator) ReflectionSupport.newInstance(clazz))
							.map(Aggregator::new)
							.orElse(Aggregator.DEFAULT);
				} // @formatter:on
				catch (Exception ex) {
					throw parameterResolutionException("Error creating ArgumentsAggregator", ex, parameterContext);
				}
			}
		};

		abstract Resolver createResolver(ParameterContext parameterContext);

	}

	interface Resolver {

		Object resolve(ParameterContext parameterContext, Object[] arguments);

	}

	static class Converter implements Resolver {

		private static final Converter DEFAULT = new Converter(DefaultArgumentConverter.INSTANCE);

		private final ArgumentConverter argumentConverter;

		Converter(ArgumentConverter argumentConverter) {
			this.argumentConverter = argumentConverter;
		}

		@Override
		public Object resolve(ParameterContext parameterContext, Object[] arguments) {
			Object argument = arguments[parameterContext.getIndex()];
			try {
				return this.argumentConverter.convert(argument, parameterContext);
			}
			catch (Exception ex) {
				throw parameterResolutionException("Error converting parameter", ex, parameterContext);
			}
		}

	}

	static class Aggregator implements Resolver {

		private static final Aggregator DEFAULT = new Aggregator((accessor, context) -> accessor);

		private final ArgumentsAggregator argumentsAggregator;

		Aggregator(ArgumentsAggregator argumentsAggregator) {
			this.argumentsAggregator = argumentsAggregator;
		}

		@Override
		public Object resolve(ParameterContext parameterContext, Object[] arguments) {
			ArgumentsAccessor accessor = new DefaultArgumentsAccessor(arguments);
			try {
				return this.argumentsAggregator.aggregateArguments(accessor, parameterContext);
			}
			catch (Exception ex) {
				throw parameterResolutionException("Error aggregating arguments for parameter", ex, parameterContext);
			}
		}

	}

	private static ParameterResolutionException parameterResolutionException(String message, Exception cause,
			ParameterContext parameterContext) {
		String fullMessage = message + " at index " + parameterContext.getIndex();
		if (StringUtils.isNotBlank(cause.getMessage())) {
			fullMessage += ": " + cause.getMessage();
		}
		return new ParameterResolutionException(fullMessage, cause);
	}

}
