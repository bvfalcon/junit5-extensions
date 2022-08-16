package name.bychkov.junit5.params;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.util.AnnotationUtils;

/**
 * @since 5.0
 */
class ParameterizedTemplateParameterResolver implements ParameterResolver, AfterTestExecutionCallback {

	private static final Namespace NAMESPACE = Namespace.create(ParameterizedTemplateParameterResolver.class);

	private final ParameterizedTemplateClassContext classContext;
	private final Object[] arguments;

	ParameterizedTemplateParameterResolver(ParameterizedTemplateClassContext classContext, Object[] arguments) {
		this.classContext = classContext;
		this.arguments = arguments;
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		Executable declaringExecutable = parameterContext.getDeclaringExecutable();
		Method testMethod = extensionContext.getTestMethod().orElse(null);
		int parameterIndex = parameterContext.getIndex();

		// Not a @ParameterizedTest method?
		if (!declaringExecutable.equals(testMethod)) {
			return false;
		}

		// Current parameter is an aggregator?
		if (this.classContext.isAggregator(parameterIndex)) {
			return true;
		}

		// Ensure that the current parameter is declared before aggregators.
		// Otherwise, a different ParameterResolver should handle it.
		if (this.classContext.hasAggregator()) {
			return parameterIndex < this.classContext.indexOfFirstAggregator();
		}

		// Else fallback to behavior for parameterized test methods without aggregators.
		return parameterIndex < this.arguments.length;
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		return this.classContext.resolve(parameterContext, extractPayloads(this.arguments));
	}

	/**
	 * @since 5.8
	 */
	@Override
	public void afterTestExecution(ExtensionContext context) {
		ParameterizedTemplate parameterizedTemplate = AnnotationUtils.findAnnotation(context.getRequiredTestClass(),
			ParameterizedTemplate.class).get();
		if (!parameterizedTemplate.autoCloseArguments()) {
			return;
		}

		Store store = context.getStore(NAMESPACE);
		AtomicInteger argumentIndex = new AtomicInteger();

		Arrays.stream(this.arguments) //
				.filter(AutoCloseable.class::isInstance) //
				.map(AutoCloseable.class::cast) //
				.map(CloseableArgument::new) //
				.forEach(closeable -> store.put("closeableArgument#" + argumentIndex.incrementAndGet(), closeable));
	}

	private static class CloseableArgument implements Store.CloseableResource {

		private final AutoCloseable autoCloseable;

		CloseableArgument(AutoCloseable autoCloseable) {
			this.autoCloseable = autoCloseable;
		}

		@Override
		public void close() throws Throwable {
			this.autoCloseable.close();
		}

	}

	private Object[] extractPayloads(Object[] arguments) {
		return Arrays.stream(arguments) //
				.map(argument -> {
					if (argument instanceof Named) {
						return ((Named<?>) argument).getPayload();
					}
					return argument;
				}) //
				.toArray();
	}

}
