package name.bychkov.junit5.params;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code @ParameterizedConstructor} is used to signal that the annotated constructor is a
 * parameterized constructor.
 * 
 * @see org.junit.jupiter.params.ParameterizedTest
 * @see org.junit.jupiter.params.provider.Arguments
 * @see name.bychkov.junit5.params.provider.ValueSource
 * @see name.bychkov.junit5.params.provider.MethodSource
 * @see name.bychkov.junit5.params.provider.EnumSource
 * @see name.bychkov.junit5.params.provider.EmptySource
 * @see name.bychkov.junit5.params.provider.NullSource
 * 
 * */
@Target(ElementType.CONSTRUCTOR)
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface ParameterizedConstructor {

	String DISPLAY_NAME_PLACEHOLDER = "{displayName}";

	String INDEX_PLACEHOLDER = "{index}";

	String ARGUMENTS_PLACEHOLDER = "{arguments}";

	String ARGUMENTS_WITH_NAMES_PLACEHOLDER = "{argumentsWithNames}";

	String DEFAULT_DISPLAY_NAME = "[" + INDEX_PLACEHOLDER + "] " + ARGUMENTS_WITH_NAMES_PLACEHOLDER;

	String name() default "{default_display_name}";

	boolean autoCloseArguments() default true;
}
