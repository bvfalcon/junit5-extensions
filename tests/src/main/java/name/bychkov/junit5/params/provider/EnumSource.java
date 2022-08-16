package name.bychkov.junit5.params.provider;

import static java.util.stream.Collectors.toSet;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.util.Preconditions;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ArgumentsSource(EnumArgumentsProvider.class)
public @interface EnumSource {

	Class<? extends Enum<?>> value() default NullEnum.class;

	String[] names() default {};

	Mode mode() default Mode.INCLUDE;

	enum Mode {

		INCLUDE(Mode::validateNames, (name, names) -> names.contains(name)),

		EXCLUDE(Mode::validateNames, (name, names) -> !names.contains(name)),

		MATCH_ALL(Mode::validatePatterns, (name, patterns) -> patterns.stream().allMatch(name::matches)),

		MATCH_ANY(Mode::validatePatterns, (name, patterns) -> patterns.stream().anyMatch(name::matches)),

		MATCH_NONE(Mode::validatePatterns, (name, patterns) -> patterns.stream().noneMatch(name::matches));

		private final Validator validator;
		private final BiPredicate<String, Set<String>> selector;

		Mode(Validator validator, BiPredicate<String, Set<String>> selector) {
			this.validator = validator;
			this.selector = selector;
		}

		void validate(EnumSource enumSource, Set<? extends Enum<?>> constants, Set<String> names) {
			Preconditions.notNull(enumSource, "EnumSource must not be null");
			Preconditions.notNull(names, "names must not be null");

			validator.validate(enumSource, constants, names);
		}

		boolean select(Enum<?> constant, Set<String> names) {
			Preconditions.notNull(constant, "Enum constant must not be null");
			Preconditions.notNull(names, "names must not be null");

			return selector.test(constant.name(), names);
		}

		private static void validateNames(EnumSource enumSource, Set<? extends Enum<?>> constants, Set<String> names) {
			Set<String> allNames = constants.stream().map(Enum::name).collect(toSet());
			Preconditions.condition(allNames.containsAll(names),
				() -> "Invalid enum constant name(s) in " + enumSource + ". Valid names include: " + allNames);
		}

		private static void validatePatterns(EnumSource enumSource, Set<? extends Enum<?>> constants,
				Set<String> names) {
			try {
				names.forEach(Pattern::compile);
			}
			catch (PatternSyntaxException e) {
				throw new PreconditionViolationException(
					"Pattern compilation failed for a regular expression supplied in " + enumSource, e);
			}
		}

		private interface Validator {
			void validate(EnumSource enumSource, Set<? extends Enum<?>> constants, Set<String> names);
		}

	}

}
