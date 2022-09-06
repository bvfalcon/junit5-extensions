package name.bychkov.junit5.params;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExecutableInvoker;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstances;
import org.junit.jupiter.api.parallel.ExecutionMode;

public class ParameterizedConstructorExecutionContext implements ExtensionContext
{
	private Constructor<?> constructor;
	
	public ParameterizedConstructorExecutionContext(Constructor<?> constructor)
	{
		this.constructor = constructor;
	}
	
	@Override
	public Optional<ExtensionContext> getParent()
	{
		return Optional.empty();
	}
	
	@Override
	public ExtensionContext getRoot()
	{
		return null;
	}
	
	@Override
	public String getUniqueId()
	{
		return null;
	}
	
	@Override
	public String getDisplayName()
	{
		return null;
	}
	
	@Override
	public Set<String> getTags()
	{
		return null;
	}
	
	@Override
	public Optional<AnnotatedElement> getElement()
	{
		return Optional.of(constructor);
	}
	
	@Override
	public Optional<Class<?>> getTestClass()
	{
		return Optional.of(constructor.getDeclaringClass());
	}
	
	@Override
	public Optional<Lifecycle> getTestInstanceLifecycle()
	{
		return Optional.empty();
	}
	
	@Override
	public Optional<Object> getTestInstance()
	{
		return Optional.empty();
	}
	
	@Override
	public Optional<TestInstances> getTestInstances()
	{
		return Optional.empty();
	}
	
	@Override
	public Optional<Method> getTestMethod()
	{
		return Optional.empty();
	}
	
	@Override
	public Optional<Throwable> getExecutionException()
	{
		return Optional.empty();
	}
	
	@Override
	public Optional<String> getConfigurationParameter(String key)
	{
		return Optional.empty();
	}
	
	@Override
	public <T> Optional<T> getConfigurationParameter(String key, Function<String, T> transformer)
	{
		return Optional.empty();
	}
	
	@Override
	public void publishReportEntry(Map<String, String> map)
	{
	}
	
	@Override
	public Store getStore(Namespace namespace)
	{
		return null;
	}
	
	@Override
	public ExecutionMode getExecutionMode()
	{
		return null;
	}
	
	@Override
	public ExecutableInvoker getExecutableInvoker()
	{
		return null;
	}
	
}
