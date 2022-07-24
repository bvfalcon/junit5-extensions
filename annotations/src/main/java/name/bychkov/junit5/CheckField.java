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
@Repeatable(CheckField.List.class)
@Documented
public @interface CheckField
{
	/**
	 * Class with target field to check <br />
	 * Required attribute 
	 * */
	Class<?> targetClass();
	
	/**
	 * Field name <br />
	 * Required attribute
	 * */
	String value();
	
	/**
	 * Type of field <br />
	 * Optional attribute
	 * */
	Class<?> type() default NULL.class;
	
	static final class NULL {}
	
	/**
	 * Custom message if field not exists/not accessible <br />
	 * Optional attribute
	 * */
	String message() default "";
	
	@Retention(SOURCE)
	@Target({ TYPE, CONSTRUCTOR, FIELD, METHOD })
	@Documented
	@interface List
	{
		/**
		 * Array of @CheckField annotations
		 * */
		CheckField[] value();
	}
}