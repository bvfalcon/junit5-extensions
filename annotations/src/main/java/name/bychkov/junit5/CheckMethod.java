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
@Repeatable(CheckMethod.List.class)
@Documented
public @interface CheckMethod
{
	/**
	 * Class with target method to check <br />
	 * Required attribute 
	 * */
	Class<?> targetClass();
	
	/**
	 * Method name <br />
	 * Required attribute
	 * */
	String value();
	
	/**
	 * Type of object method returns <br />
	 * Optional attribute <br />
	 * Checking only if defined
	 * */
	Class<?> returnType() default NULL.class;
	
	static final class NULL {}
	
	/**
	 * Array of method parameter classes <br />
	 * Optional attribute <br />
	 * Checking only if defined
	 * */
	Class<?>[] parameters() default NULL.class;
	
	/**
	 * Custom message if method not exists/not accessible <br />
	 * Optional attribute
	 * */
	String message() default "";
	
	@Retention(SOURCE)
	@Target({ TYPE, CONSTRUCTOR, FIELD, METHOD })
	@Documented
	@interface List
	{
		/**
		 * Array of @CheckMethod annotations
		 * */
		CheckMethod[] value();
	}
}