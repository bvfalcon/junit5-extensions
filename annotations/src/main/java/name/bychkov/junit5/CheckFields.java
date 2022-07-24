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
@Repeatable(CheckFields.List.class)
@Documented
public @interface CheckFields
{
	/**
	 * Class with target fields to check <br />
	 * Required attribute 
	 * */
	Class<?> targetClass();
	
	/**
	 * Array of field names <br />
	 * Required attribute
	 * */
	String[] values();
	
	/**
	 * Custom message if some fields not exists/not accessible <br />
	 * Optional attribute
	 * */
	String message() default "";
	
	@Retention(SOURCE)
	@Target({ TYPE, CONSTRUCTOR, FIELD, METHOD })
	@Documented
	@interface List
	{
		/**
		 * Array of @CheckFields annotations
		 * */
		CheckFields[] value();
	}
}