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
@Repeatable(CheckResourceBundle.List.class)
@Documented
public @interface CheckResourceBundle
{
	/**
	 * base name of resource bundle <br />
	 * Required attribute
	 * */
	String baseName();
	
	/**
	 * Array of locale names to check <br />
	 * Required attribute <br />
	 * Note: default locale is locale with empty ("") name. It can be defined in locales array too: {"", "en", ...}
	 * */
	String[] locales();
	
	/**
	 * Custom message if some keys in resource bundle are not synchronized <br />
	 * Optional attribute
	 * */
	String message() default "";
	
	@Retention(SOURCE)
	@Target({ TYPE, CONSTRUCTOR, FIELD, METHOD })
	@Documented
	@interface List
	{
		/**
		 * Array of @CheckResourceBundle annotations
		 * */
		CheckResourceBundle[] value();
	}
}