package name.bychkov.junit5;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target({ TYPE, CONSTRUCTOR, FIELD, METHOD })
@Repeatable(CheckExistence.List.class)
public @interface CheckExistence
{
	Class<?> targetClass();
	
	Class<?>[] constructorParameters() default {};
	
	String method() default "";
	Class<?>[] methodParameters() default {};
	
	String field() default "";
	
	String message() default "";
	
	@Retention(RUNTIME)
	@Target({ TYPE, CONSTRUCTOR, FIELD, METHOD })
	@interface List
	{
		CheckExistence[] value();
	}
}