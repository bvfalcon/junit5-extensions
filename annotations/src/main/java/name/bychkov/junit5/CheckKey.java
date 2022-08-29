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

/**
 * Check existence of specified key in specified resource bundle with default (empty) or specified locale
 * 
 * @author Vladimir V. Bychkov
 * */
@Retention(SOURCE)
@Target({ TYPE, CONSTRUCTOR, FIELD, METHOD })
@Repeatable(CheckKey.List.class)
@Documented
public @interface CheckKey
{
	/**
	 * base name of resource bundle.<br />
	 * 
	 * This is required field except when this annotation is applied to constant (static final) field.
	 * In this special case constant value as value of this annotation field will be used.
	 * 
	 * @see java.util.ResourceBundle
	 * */
	String baseName()  default "";
	
	/**
	 * name of key in resource bundle.<br />
	 * 
	 * This is required field except when this annotation is applied to constant (static final) field.
	 * In this special case constant value as value of this annotation field will be used.
	 * */
	String value() default "";
	
	/**
	 * Locale name
	 * @see java.util.Locale#forLanguageTag(String)
	 * */
	String locale() default "";
	
	/**
	 * Custom message if key not exists
	 * */
	String message() default "";
	
	@Retention(SOURCE)
	@Target({ TYPE, CONSTRUCTOR, FIELD, METHOD })
	@Documented
	@interface List
	{
		/**
		 * Array of {@link CheckKey} annotations
		 * */
		CheckKey[] value();
	}
}