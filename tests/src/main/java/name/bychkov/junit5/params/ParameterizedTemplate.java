package name.bychkov.junit5.params;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@TestTemplate
@ExtendWith(ParameterizedTemplateExtension.class)
public @interface ParameterizedTemplate {

	String DISPLAY_NAME_PLACEHOLDER = "{displayName}";

	String INDEX_PLACEHOLDER = "{index}";

	String ARGUMENTS_PLACEHOLDER = "{arguments}";

	String ARGUMENTS_WITH_NAMES_PLACEHOLDER = "{argumentsWithNames}";

	String DEFAULT_DISPLAY_NAME = "[" + INDEX_PLACEHOLDER + "] " + ARGUMENTS_WITH_NAMES_PLACEHOLDER;

	String name() default "{default_display_name}";

	boolean autoCloseArguments() default true;
}
