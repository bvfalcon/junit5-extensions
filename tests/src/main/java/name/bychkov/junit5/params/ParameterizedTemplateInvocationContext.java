package name.bychkov.junit5.params;

import static java.util.Collections.singletonList;

import java.util.List;

import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;

class ParameterizedTemplateInvocationContext implements TestTemplateInvocationContext {

	private final ParameterizedTemplateNameFormatter formatter;
	private final ParameterizedTemplateClassContext classContext;
	private final Object[] arguments;

	ParameterizedTemplateInvocationContext(ParameterizedTemplateNameFormatter formatter,
			ParameterizedTemplateClassContext classContext, Object[] arguments) {
		this.formatter = formatter;
		this.classContext = classContext;
		this.arguments = arguments;
	}

	@Override
	public String getDisplayName(int invocationIndex) {
		return this.formatter.format(invocationIndex, this.arguments);
	}

	@Override
	public List<Extension> getAdditionalExtensions() {
		return singletonList(new ParameterizedTemplateParameterResolver(this.classContext, this.arguments));
	}

}
