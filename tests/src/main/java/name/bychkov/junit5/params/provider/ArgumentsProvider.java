package name.bychkov.junit5.params.provider;

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;

public interface ArgumentsProvider {

	Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception;

}
