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
 * Check existence/availability of specified constructor in targetClass
 * 
 * @author Vladimir V. Bychkov
 * */
@Retention(SOURCE)
@Target({ TYPE, CONSTRUCTOR, FIELD, METHOD, LOCAL_VARIABLE })
@Repeatable(CheckConstructor.List.class)
@Documented
public @interface CheckConstructor
{
	/**
	 * Class with target constructor to check
	 * */
	Class<?> targetClass();

	/**
	 * Array of constructor parameter classes <br />
	 * Note: if parameters are not specified, existence of default constructor (without parameters) will be checked.
	 * */
	Class<?>[] parameters() default {};
	
	/**
	 * Custom message if constructor not exists/not accessible
	 * */
	String message() default "";
	
	@Retention(SOURCE)
	@Target({ TYPE, CONSTRUCTOR, FIELD, METHOD, LOCAL_VARIABLE })
	@Documented
	@interface List
	{
		/**
		 * Array of {@link CheckConstructor} annotations
		 * */
		CheckConstructor[] value();
	}
}