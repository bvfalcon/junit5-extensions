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
@Repeatable(CheckKey.List.class)
@Documented
public @interface CheckKey
{
	/**
	 * base name of resource bundle <br />
	 * Required attribute
	 * */
	String baseName();
	
	/**
	 * name of key in resource bundle <br />
	 * Required attribute
	 * */
	String value();
	
	/**
	 * Locale name <br />
	 * Optional attribute, default empty
	 * */
	String locale() default "";
	
	/**
	 * Custom message if key not exists <br />
	 * Optional attribute
	 * */
	String message() default "";
	
	@Retention(SOURCE)
	@Target({ TYPE, CONSTRUCTOR, FIELD, METHOD })
	@Documented
	@interface List
	{
		/**
		 * Array of @CheckKey annotations
		 * */
		CheckKey[] value();
	}
}