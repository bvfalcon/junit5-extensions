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
 * Check existence of specified key in specified resource bundle with default (empty) or specified locale.<br />
 * 
 * <p>This annotation can be used in different ways:
 * <ul>
 * <li> basic usage
 * <li> short form
 * </ul>
 * <p>In basic usage scenario fields baseName and value are mandatory. Annotation can be applied to classes, interfaces, constructor, fields and methods. For example:
 * 
 * <blockquote><pre>
 * {@literal @}CheckKey(baseName="Messages", value="title")
 * public class SomeClass {}
 * </pre></blockquote>
 * 
 * <p>Short form can be used in case of applying annotation to constant (static final) field type of String. In this case value of constant field will be used as value of absent annotations field. For example:
 * 
 * <blockquote><pre>
 * public class SomeClass {
 * 
 *     {@literal @}CheckKey(baseName="Messages")
 *     private static final String KEY_NAME = "title";
 * 
 *     {@literal @}CheckKey(value="title")
 *     private static final String RESOURCE_BUNDLE_BASE_NAME = "Messages";
 * 
 * }
 * </pre></blockquote>
 * 
 * <p>Note, it is not allowed to leave blank both baseName and value.
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