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
@Repeatable(CheckMethod.List.class)
public @interface CheckMethod
{
	Class<?> targetClass();
	
	String value();
	
	Class<?>[] parameters() default {};
	
	Class<?> returnType();
	
	String message() default "";
	
	@Retention(RUNTIME)
	@Target({ TYPE, CONSTRUCTOR, FIELD, METHOD })
	@interface List
	{
		CheckMethod[] value();
	}
}