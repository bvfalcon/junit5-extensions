package name.bychkov.junit5.params;

import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;
import static org.junit.platform.commons.util.AnnotationUtils.findRepeatableAnnotations;
import static org.junit.platform.commons.util.AnnotationUtils.isAnnotated;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.support.AnnotationConsumerInitializer;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;

import name.bychkov.junit5.params.provider.ArgumentsProvider;
import name.bychkov.junit5.params.provider.ArgumentsSource;

class ParameterizedTemplateExtension implements TestTemplateInvocationContextProvider {

	private static final String CLASS_CONTEXT_KEY = "context";
	static final String ARGUMENT_MAX_LENGTH_KEY = "junit.jupiter.params.displayname.argument.maxlength";
	private static final String DEFAULT_DISPLAY_NAME = "{default_display_name}";
	static final String DISPLAY_NAME_PATTERN_KEY = "junit.jupiter.params.displayname.default";

	@Override
	public boolean supportsTestTemplate(ExtensionContext context) {
		if (!context.getTestClass().isPresent()) {
			return false;
		}

		Class<?> testClass = context.getTestClass().get();
		if (!isAnnotated(testClass, ParameterizedTemplate.class)) {
			return false;
		}

		ParameterizedTemplateClassContext classContext = new ParameterizedTemplateClassContext(testClass);

		Preconditions.condition(classContext.hasPotentiallyValidSignature(),
			() -> String.format(
				"@ParameterizedTemplate class [%s] declares formal parameters in an invalid order: "
						+ "argument aggregators must be declared after any indexed arguments "
						+ "and before any arguments resolved by another ParameterResolver.",
				testClass.toGenericString()));

		getStore(context).put(CLASS_CONTEXT_KEY, classContext);

		return true;
	}

	@Override
	public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(
			ExtensionContext extensionContext) {

		Class<?> templateClass = extensionContext.getRequiredTestClass();
		String displayName = extensionContext.getDisplayName();
		ParameterizedTemplateClassContext classContext = getStore(extensionContext)
				.get(CLASS_CONTEXT_KEY, ParameterizedTemplateClassContext.class);
		int argumentMaxLength = extensionContext.getConfigurationParameter(ARGUMENT_MAX_LENGTH_KEY,
			Integer::parseInt).orElse(512);
		ParameterizedTemplateNameFormatter formatter = createNameFormatter(extensionContext, templateClass, classContext,
			displayName, argumentMaxLength);
		AtomicLong invocationCount = new AtomicLong(0);

		// @formatter:off
		return findRepeatableAnnotations(templateClass, ArgumentsSource.class)
				.stream()
				.map(ArgumentsSource::value)
				.map(this::instantiateArgumentsProvider)
				.map(provider -> AnnotationConsumerInitializer.initialize(templateClass, provider))
				.flatMap(provider -> arguments(provider, extensionContext))
				.map(Arguments::get)
				.map(arguments -> consumedArguments(arguments, classContext))
				.map(arguments -> {
					invocationCount.incrementAndGet();
					return createInvocationContext(formatter, classContext, arguments);
				})
				.onClose(() ->
						Preconditions.condition(invocationCount.get() > 0,
								"Configuration error: You must configure at least one set of arguments for this @ParameterizedTemplate"));
		// @formatter:on
	}

	private ArgumentsProvider instantiateArgumentsProvider(Class<? extends ArgumentsProvider> clazz) {
		try {
			return ReflectionUtils.newInstance(clazz);
		}
		catch (Exception ex) {
			if (ex instanceof NoSuchMethodException) {
				String message = String.format("Failed to find a no-argument constructor for ArgumentsProvider [%s]. "
						+ "Please ensure that a no-argument constructor exists and "
						+ "that the class is either a top-level class or a static nested class",
					clazz.getName());
				throw new JUnitException(message, ex);
			}
			throw ex;
		}
	}

	private ExtensionContext.Store getStore(ExtensionContext context) {
		return context.getStore(Namespace.create(ParameterizedTemplateExtension.class, context.getRequiredTestMethod()));
	}

	private TestTemplateInvocationContext createInvocationContext(ParameterizedTemplateNameFormatter formatter,
			ParameterizedTemplateClassContext classContext, Object[] arguments) {
		return new ParameterizedTemplateInvocationContext(formatter, classContext, arguments);
	}

	private ParameterizedTemplateNameFormatter createNameFormatter(ExtensionContext extensionContext, Class<?> templateClass,
			ParameterizedTemplateClassContext classContext, String displayName, int argumentMaxLength) {
		ParameterizedTemplate parameterizedTemplate = findAnnotation(templateClass, ParameterizedTemplate.class).get();
		String pattern = parameterizedTemplate.name().equals(DEFAULT_DISPLAY_NAME)
				? extensionContext.getConfigurationParameter(DISPLAY_NAME_PATTERN_KEY).orElse(
					ParameterizedTemplate.DEFAULT_DISPLAY_NAME)
				: parameterizedTemplate.name();
		pattern = Preconditions.notBlank(pattern.trim(),
			() -> String.format(
				"Configuration error: @ParameterizedTemplate on class [%s] must be declared with a non-empty name.",
				templateClass));
		return new ParameterizedTemplateNameFormatter(pattern, displayName, classContext, argumentMaxLength);
	}

	protected static Stream<? extends Arguments> arguments(ArgumentsProvider provider, ExtensionContext context) {
		try {
			return provider.provideArguments(context);
		}
		catch (Exception e) {
			throw ExceptionUtils.throwAsUncheckedException(e);
		}
	}

	private Object[] consumedArguments(Object[] arguments, ParameterizedTemplateClassContext classContext) {
		int parameterCount = classContext.getParameterCount();
		if (classContext.hasAggregator()) {
			return arguments;
		}
		return arguments.length > parameterCount ? Arrays.copyOf(arguments, parameterCount) : arguments;
	}

}
