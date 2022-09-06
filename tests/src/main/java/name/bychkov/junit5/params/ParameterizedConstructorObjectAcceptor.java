package name.bychkov.junit5.params;

import org.junit.jupiter.params.provider.ArgumentsProvider;

import name.bychkov.junit5.params.ParameterizedConstructorAnnotationProcessor.ParameterizedConstructorObject;

public interface ParameterizedConstructorObjectAcceptor extends ArgumentsProvider
{
	public void accept(ParameterizedConstructorObject object);
}
