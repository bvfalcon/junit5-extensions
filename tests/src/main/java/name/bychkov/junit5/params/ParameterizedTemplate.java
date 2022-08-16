package name.bychkov.junit5.params;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@TestTemplate
@ExtendWith(ParameterizedTemplateExtension.class)
public @interface ParameterizedTemplate {

	/**
	 * Placeholder for the {@linkplain org.junit.jupiter.api.TestInfo#getDisplayName
	 * display name} of a {@code @ParameterizedTest} method: <code>{displayName}</code>
	 *
	 * @since 5.3
	 * @see #name
	 */
	String DISPLAY_NAME_PLACEHOLDER = "{displayName}";

	/**
	 * Placeholder for the current invocation index of a {@code @ParameterizedTest}
	 * method (1-based): <code>{index}</code>
	 *
	 * @since 5.3
	 * @see #name
	 */
	String INDEX_PLACEHOLDER = "{index}";

	/**
	 * Placeholder for the complete, comma-separated arguments list of the
	 * current invocation of a {@code @ParameterizedTest} method:
	 * <code>{arguments}</code>
	 *
	 * @since 5.3
	 * @see #name
	 */
	String ARGUMENTS_PLACEHOLDER = "{arguments}";

	/**
	 * Placeholder for the complete, comma-separated named arguments list
	 * of the current invocation of a {@code @ParameterizedTest} method:
	 * <code>{argumentsWithNames}</code>
	 *
	 * <p>Argument names will be retrieved via the {@link java.lang.reflect.Parameter#getName()}
	 * API if the byte code contains parameter names &mdash; for example, if
	 * the code was compiled with the {@code -parameters} command line argument
	 * for {@code javac}.
	 *
	 * @since 5.6
	 * @see #name
	 */
	String ARGUMENTS_WITH_NAMES_PLACEHOLDER = "{argumentsWithNames}";

	/**
	 * Default display name pattern for the current invocation of a
	 * {@code @ParameterizedTest} method: {@value}
	 *
	 * <p>Note that the default pattern does <em>not</em> include the
	 * {@linkplain #DISPLAY_NAME_PLACEHOLDER display name} of the
	 * {@code @ParameterizedTest} method.
	 *
	 * @since 5.3
	 * @see #name
	 * @see #DISPLAY_NAME_PLACEHOLDER
	 * @see #INDEX_PLACEHOLDER
	 * @see #ARGUMENTS_WITH_NAMES_PLACEHOLDER
	 */
	String DEFAULT_DISPLAY_NAME = "[" + INDEX_PLACEHOLDER + "] " + ARGUMENTS_WITH_NAMES_PLACEHOLDER;

	/**
	 * The display name to be used for individual invocations of the
	 * parameterized test; never blank or consisting solely of whitespace.
	 *
	 * <p>Defaults to <code>{default_display_name}</code>.
	 *
	 * <p>If the default display name flag (<code>{default_display_name}</code>)
	 * is not overridden, JUnit will:
	 * <ul>
	 * <li>Look up the {@value ParameterizedTestExtension#DISPLAY_NAME_PATTERN_KEY}
	 * <em>configuration parameter</em> and use it if available. The configuration
	 * parameter can be supplied via the {@code Launcher} API, build tools (e.g.,
	 * Gradle and Maven), a JVM system property, or the JUnit Platform configuration
	 * file (i.e., a file named {@code junit-platform.properties} in the root of
	 * the class path). Consult the User Guide for further information.</li>
	 * <li>Otherwise, the value of the {@link #DEFAULT_DISPLAY_NAME} constant will
	 * be used.</li>
	 * </ul>
	 *
	 * <h4>Supported placeholders</h4>
	 * <ul>
	 * <li>{@link #DISPLAY_NAME_PLACEHOLDER}</li>
	 * <li>{@link #INDEX_PLACEHOLDER}</li>
	 * <li>{@link #ARGUMENTS_PLACEHOLDER}</li>
	 * <li>{@link #ARGUMENTS_WITH_NAMES_PLACEHOLDER}</li>
	 * <li><code>{0}</code>, <code>{1}</code>, etc.: an individual argument (0-based)</li>
	 * </ul>
	 *
	 * <p>For the latter, you may use {@link java.text.MessageFormat} patterns
	 * to customize formatting. Please note that the original arguments are
	 * passed when formatting, regardless of any implicit or explicit argument
	 * conversions.
	 *
	 * <p>Note that <code>{default_display_name}</code> is a flag rather than a
	 * placeholder.
	 *
	 * @see java.text.MessageFormat
	 */
	String name() default "{default_display_name}";

	/**
	 * Configure whether all arguments of the parameterized test that implement {@link AutoCloseable}
	 * will be closed after {@link org.junit.jupiter.api.AfterEach @AfterEach} methods
	 * and {@link org.junit.jupiter.api.extension.AfterEachCallback AfterEachCallback}
	 * extensions have been called for the current parameterized test invocation.
	 *
	 * <p>Defaults to {@code true}.
	 *
	 * <p><strong>WARNING</strong>: if an argument that implements {@code AutoCloseable}
	 * is reused for multiple invocations of the same parameterized test method,
	 * you must set {@code autoCloseArguments} to {@code false} to ensure that
	 * the argument is not closed between invocations.
	 *
	 * @since 5.8
	 * @see java.lang.AutoCloseable
	 */
	@API(status = EXPERIMENTAL, since = "5.8")
	boolean autoCloseArguments() default true;

}
