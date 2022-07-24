package name.bychkov.junit5;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(SOURCE)
@Target({ TYPE, CONSTRUCTOR, FIELD, METHOD })
@Repeatable(CheckConstructor.List.class)
@Documented
public @interface CheckConstructor
{
	/**
	 * Class with target constructor to check <br />
	 * Required attribute 
	 * */
	Class<?> targetClass();

	/**
	 * Array of constructor parameter classes <br />
	 * Optional attribute, default empty array <br />
	 * Note: if parameters are not defined, existance of default constructor (without parameters) will be checked.
	 * */
	Class<?>[] parameters() default {};
	
	/**
	 * Custom message if constructor not exists/not accessible <br />
	 * Optional attribute
	 * */
	String message() default "";
	
	@Retention(SOURCE)
	@Target({ TYPE, CONSTRUCTOR, FIELD, METHOD })
	@Documented
	@interface List
	{
		/**
		 * Array of @CheckConstructor annotations
		 * */
		CheckConstructor[] value();
	}
}