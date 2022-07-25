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
 * Check existence/availability of specified field in targetClass
 * 
 * @author Vladimir V. Bychkov
 * */
@Retention(SOURCE)
@Target({ TYPE, CONSTRUCTOR, FIELD, METHOD, LOCAL_VARIABLE })
@Repeatable(CheckField.List.class)
@Documented
public @interface CheckField
{
	/**
	 * Class with target field to check
	 * */
	Class<?> targetClass();
	
	/**
	 * Field name
	 * */
	String value();
	
	/**
	 * Type of field
	 * */
	Class<?> type() default NULL.class;
	
	/**
	 * null-value if attribute is not specified
	 * */
	static final class NULL {}
	
	/**
	 * Custom message if field not exists/not accessible
	 * */
	String message() default "";
	
	@Retention(SOURCE)
	@Target({ TYPE, CONSTRUCTOR, FIELD, METHOD, LOCAL_VARIABLE })
	@Documented
	@interface List
	{
		/**
		 * Array of {@link CheckField} annotations
		 * */
		CheckField[] value();
	}
}