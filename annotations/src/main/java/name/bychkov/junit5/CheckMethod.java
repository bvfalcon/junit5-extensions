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
 * Check existence/availability of specified method in targetClass
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