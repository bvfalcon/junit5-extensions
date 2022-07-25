package name.bychkov.junit5;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Check keys synchronicity of specified resource bundle with specified locales array
 * 
 * @author Vladimir V. Bychkov
 * */
@Retention(SOURCE)
@Target({ TYPE, CONSTRUCTOR, FIELD, METHOD, LOCAL_VARIABLE })
@Repeatable(CheckResourceBundle.List.class)
@Documented
public @interface CheckResourceBundle
{
	/**
	 * base name of resource bundle
	 * @see java.util.ResourceBundle
	 * */
	String baseName();
	
	/**
	 * Array of locale names to check <br />
	 * Note: default locale is locale with empty ("") name. It can be specified in locales array too: {"", "en", ...}
	 * @see java.util.Locale#forLanguageTag(String)
	 * */
	String[] locales();
	
	/**
	 * Custom message if some keys in resource bundle are not synchronized
	 * */
	String message() default "";
	
	@Retention(SOURCE)
	@Target({ TYPE, CONSTRUCTOR, FIELD, METHOD, LOCAL_VARIABLE })
	@Documented
	@interface List
	{
		/**
		 * Array of {@link CheckResourceBundle} annotations
		 * */
		CheckResourceBundle[] value();
	}
}