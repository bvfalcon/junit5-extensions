package name.bychkov.junit5;

import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Check all classes in specified targetPackage: they must be serializable (implements java.io.Serializable)
 * 
 * @author Vladimir V. Bychkov
 * */
@Retention(SOURCE)
@Target({ PACKAGE })
@Repeatable(CheckSerializable.List.class)
@Documented
public @interface CheckSerializable
{
	/**
	 * package with classes to check <br />
	 * 
	 * by default current package
	 * */
	String targetPackage() default "";

	/**
	 * Array of classes to exclude
	 * */
	Class<?>[] excludes() default {};
	
	/**
	 * Custom message if any classes are not serializable
	 * */
	String message() default "";
	
	@Retention(SOURCE)
	@Target({ PACKAGE })
	@Documented
	@interface List
	{
		/**
		 * Array of {@link CheckSerializable} annotations
		 * */
		CheckSerializable[] value();
	}
}