package name.bychkov.junit5.params.provider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.params.provider.ArgumentsSource;

@Target(ElementType.CONSTRUCTOR)
@Retention(RetentionPolicy.RUNTIME)
@ArgumentsSource(ValueArgumentsProvider.class)
public @interface ValueSource
{
	short[] shorts() default {};
	
	byte[] bytes() default {};
	
	int[] ints() default {};
	
	long[] longs() default {};
	
	float[] floats() default {};
	
	double[] doubles() default {};
	
	char[] chars() default {};
	
	boolean[] booleans() default {};
	
	String[] strings() default {};
	
	Class<?>[] classes() default {};
}
