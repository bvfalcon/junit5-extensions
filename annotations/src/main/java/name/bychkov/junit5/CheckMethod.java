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
 * Check existence/availability of specified method in targetClass.<br />
 * 
 * <p>This annotation can be used in different ways:
 * <ul>
 * <li> basic usage
 * <li> short form
 * </ul>
 * 
 * <p>In basic usage scenario fields targetClass and value are mandatory. Annotation can be applied to classes, interfaces, constructor, fields and methods. For example:
 * 
 * <blockquote><pre>
 * {@literal @}CheckMethod(targetClass=AnyObject.class, value="calculateAmount")
 * public class SomeClass {}
 * </pre></blockquote>
 * 
 * <p>Short form can be used in case of applying annotation to constant (static final) field type of String. In this case value of constant field will be used as value of annotation field. For example:
 * 
 * <blockquote><pre>
 * public class SomeClass {
 * 
 *     {@literal @}CheckMethod(targetClass=AnyObject.class)
 *     private static final String METHOD_NAME = "calculateAmount";
 * 
 * }
 * </pre></blockquote>
 * 
 * @author Vladimir V. Bychkov
 * */
@Retention(SOURCE)
@Target({ TYPE, CONSTRUCTOR, FIELD, METHOD })
@Repeatable(CheckMethod.List.class)
@Documented
public @interface CheckMethod
{
	/**
	 * Class with target method to check
	 * */
	Class<?> targetClass();
	
	/**
	 * Method name.<br />
	 * 
	 * This is required field except when this annotation is applied to constant (static final) field.
	 * In this special case constant value as value of this annotation field will be used.
	 * */
	String value() default "";
	
	/**
	 * Type of object method returns <br />
	 * Checking only if specified
	 * */
	Class<?> returnType() default NULL.class;
	
	/**
	 * null-value if attribute is not specified
	 * */
	static final class NULL {}
	
	/**
	 * Array of method parameter classes <br />
	 * Checking only if specified
	 * */
	Class<?>[] parameters() default NULL.class;
	
	/**
	 * Custom message if method not exists/not accessible
	 * */
	String message() default "";
	
	@Retention(SOURCE)
	@Target({ TYPE, CONSTRUCTOR, FIELD, METHOD })
	@Documented
	@interface List
	{
		/**
		 * Array of {@link CheckMethod} annotations
		 * */
		CheckMethod[] value();
	}
}